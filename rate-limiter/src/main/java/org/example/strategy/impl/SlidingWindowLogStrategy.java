package org.example.strategy.impl;

import java.util.Date;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import org.example.models.LimitingKey;
import org.example.models.Request;
import org.example.strategy.LimitingStrategy;

public class SlidingWindowLogStrategy implements LimitingStrategy {
    private final LimitingKey limitingKey;
    private final ScheduledExecutorService executorService;
    private final long windowLengthInMs;
    private final int windowRequestCount;
    private final ConcurrentHashMap<String, Node> persistence;

    public SlidingWindowLogStrategy(
            LimitingKey requestKey, long windowLengthInMs, int windowRequestCount) {
        if (requestKey == null) {
            throw new RuntimeException("Request Key cannot be null");
        }
        this.limitingKey = requestKey;
        this.windowLengthInMs = windowLengthInMs;
        this.windowRequestCount = windowRequestCount;
        this.executorService = Executors.newSingleThreadScheduledExecutor();
        this.executorService.schedule(this::cleanup, windowLengthInMs * 5, TimeUnit.MILLISECONDS);
        this.persistence = new ConcurrentHashMap<>();
    }

    private void cleanup() {
        this.persistence
                .entrySet()
                .removeIf(
                        (entry) -> {
                            entry.getValue()
                                    .cleanupOlderThan(new Date().getTime() - this.windowLengthInMs);
                            return entry.getValue().getWindowSize() == 0;
                        });
    }

    @Override
    public void dispose() {
        if (!this.executorService.isShutdown()) {
            this.executorService.shutdownNow();
        }
    }

    @Override
    public LimitingKey getLimitingKey() {
        return this.limitingKey;
    }

    @Override
    public boolean filter(Request request) {
        final AtomicBoolean shouldPass = new AtomicBoolean(false);
        this.persistence.compute(
                getKey(request),
                (key, node) -> {
                    if (node == null) {
                        node = new Node();
                        node.addRequest(request.getTimestamp());
                        shouldPass.set(true);
                    } else {
                        node.cleanupOlderThan(request.getTimestamp() - this.windowLengthInMs);
                        if (node.getWindowSize() < this.windowRequestCount) {
                            node.addRequest(request.getTimestamp());
                            shouldPass.set(true);
                        }
                    }
                    return node;
                });
        return shouldPass.get();
    }

    private static class Node {
        private final ConcurrentLinkedQueue<Long> requestTimestamps;

        Node() {
            this.requestTimestamps = new ConcurrentLinkedQueue<>();
        }

        void addRequest(long timestamp) {
            this.requestTimestamps.add(timestamp);
        }

        void cleanupOlderThan(long windowStart) {
            while (!this.requestTimestamps.isEmpty()
                    && this.requestTimestamps.peek() < windowStart) {
                this.requestTimestamps.poll();
            }
        }

        int getWindowSize() {
            return this.requestTimestamps.size();
        }
    }
}
