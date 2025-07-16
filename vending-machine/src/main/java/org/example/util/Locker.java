package org.example.util;

import java.time.Duration;
import java.time.Instant;
import java.util.AbstractMap;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public abstract class Locker<T> {

  private final LinkedList<String> _lockedItemsQueue;
  private final Map<String, LockItem<T>> _lockedItemsMap;

  protected Locker() {
    this._lockedItemsQueue = new LinkedList<>();
    this._lockedItemsMap = new ConcurrentHashMap<>();
  }

  public String lock(T t, Integer quantity) {
    LockItem<T> lockItem = new LockItem<>(t, quantity, UUID.randomUUID().toString(), new Date());
    this._lockedItemsQueue.add(lockItem.lockId());
    this._lockedItemsMap.put(lockItem.lockId(), lockItem);
    return lockItem.lockId();
  }

  public void commit(String lockId) {
    LockItem<T> lockItem = this._lockedItemsMap.get(lockId);
    if (lockItem == null) {
      throw new IllegalStateException("Please retry from beginning");
    }
    this._lockedItemsMap.remove(lockId);
  }

  public Map.Entry<T, Integer> rollback(String lockId) {
    LockItem<T> lockItem = this._lockedItemsMap.get(lockId);
    if (lockItem == null) {
      return null;
    }
    this._lockedItemsMap.remove(lockId);
    return new AbstractMap.SimpleImmutableEntry<>(lockItem.t(), lockItem.quantity());
  }

  protected Map<T, Integer> cleanup() {
    Map<T, Integer> cleanedUpResources = new HashMap<>();
    while (!this._lockedItemsQueue.isEmpty()) {
      LockItem<T> lockItem = this._lockedItemsMap.get(this._lockedItemsQueue.poll());
      if (lockItem == null) continue;
      if (isAtleastOneMinuteAgo(lockItem.timestamp)) {
        this.rollback(lockItem.lockId());
        cleanedUpResources.compute(
            lockItem.t(), (k, v) -> (v == null ? 0 : v) + lockItem.quantity());
      } else {
        this._lockedItemsQueue.addFirst(lockItem.lockId());
        break;
      }
    }
    return cleanedUpResources;
  }

  private boolean isAtleastOneMinuteAgo(Date date) {
    Instant instant = Instant.ofEpochMilli(date.getTime());
    Instant twentyMinutesAgo = Instant.now().minus(Duration.ofMinutes(1));
    return instant.isBefore(twentyMinutesAgo);
  }

  private record LockItem<T>(T t, Integer quantity, String lockId, Date timestamp) {}
}
