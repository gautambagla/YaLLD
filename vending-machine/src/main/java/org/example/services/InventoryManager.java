package org.example.services;

import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import org.example.models.Item;
import org.example.util.Locker;

public class InventoryManager extends Locker<Item> {

    private static InventoryManager _inventoryManager;

    public static InventoryManager getInstance() {
        if (_inventoryManager == null) _inventoryManager = new InventoryManager();
        return _inventoryManager;
    }

    private final ConcurrentHashMap<Item, Integer> _inventory;
    private final ScheduledExecutorService _cleanupScheduler;

    private InventoryManager() {
        super();
        this._inventory = new ConcurrentHashMap<>();

        // To release locked resources at periodic intervals automatically if not released by the
        // user
        this._cleanupScheduler = Executors.newScheduledThreadPool(1);
        this._cleanupScheduler.schedule(this::autoCleanup, 30L, TimeUnit.SECONDS);
    }

    private void autoCleanup() {
        super.cleanup().forEach(this::addItemToInventory);
    }

    public void addItemToInventory(Item item, int quantity) {
        this._inventory.compute(item, (k, v) -> (v == null ? 0 : v) + quantity);
    }

    public Map<Item, Integer> getAllItemsInInventory() {
        return new HashMap<>(this._inventory);
    }

    @Override
    public String lock(Item item, Integer quantity) throws NoSuchElementException {
        StringBuilder lockIdBuilder = new StringBuilder();

        // the lambda to compute() is performed atomically, this is to enhance
        // concurrent performance rather than using synchronizing this function
        this._inventory.compute(
                item,
                (k, availability) -> {
                    if (availability == null) {
                        throw new RuntimeException("Item " + item.toString() + " not found");
                    } else if (availability < quantity) {
                        throw new RuntimeException(
                                "Required quantity of Item " + item.toString() + " is unavailable");
                    }
                    lockIdBuilder.append(super.lock(item, quantity));
                    return availability - quantity;
                });
        return lockIdBuilder.toString();
    }

    @Override
    public Map.Entry<Item, Integer> rollback(String lockId) {
        Map.Entry<Item, Integer> rollbackItem = super.rollback(lockId);
        if (rollbackItem == null) return null;
        addItemToInventory(rollbackItem.getKey(), rollbackItem.getValue());
        return rollbackItem;
    }

    public void shutdown() {
        this._cleanupScheduler.shutdown();
    }
}
