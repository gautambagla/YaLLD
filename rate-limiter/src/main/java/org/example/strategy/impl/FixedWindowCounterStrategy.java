package org.example.strategy.impl;

import java.util.Date;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import lombok.Getter;
import org.example.models.LimitingKey;
import org.example.models.Request;
import org.example.strategy.LimitingStrategy;

public class FixedWindowCounterStrategy implements LimitingStrategy {
    private final LimitingKey limitingKey;
    private volatile long currentWindowTimestamp;
    private final ScheduledExecutorService executorService;
    private final int windowRequestCount;
    private final ConcurrentHashMap<String, Node> persistence;

    public FixedWindowCounterStrategy(
            LimitingKey requestKey, long windowLengthInMs, int windowRequestCount) {
        if (requestKey == null) {
            throw new RuntimeException("Request Key cannot be null");
        }
        this.limitingKey = requestKey;
        this.currentWindowTimestamp = new Date().getTime();
        this.windowRequestCount = windowRequestCount;
        this.executorService = Executors.newScheduledThreadPool(2);
        this.executorService.schedule(this::updateWindow, windowLengthInMs, TimeUnit.MILLISECONDS);
        this.executorService.schedule(this::cleanup, windowLengthInMs * 5, TimeUnit.MILLISECONDS);
        this.persistence = new ConcurrentHashMap<>();
    }

    private void cleanup() {
        persistence
                .entrySet()
                .removeIf(
                        (entry) ->
                                entry.getValue().getWindowTimestamp()
                                        != this.currentWindowTimestamp);
    }

    private void updateWindow() {
        this.currentWindowTimestamp = new Date().getTime();
    }

    @Override
    public LimitingKey getLimitingKey() {
        return this.limitingKey;
    }

    @Override
    public boolean filter(Request request) {
        final AtomicBoolean shouldPass = new AtomicBoolean(false);
        // atomic operation
        this.persistence.compute(
                getKey(request),
                (key, node) -> {
                    // if request not present or present with a different window timestamp, allow
                    if (node == null || node.getWindowTimestamp() != this.currentWindowTimestamp) {
                        shouldPass.set(true);
                        return new Node(this.currentWindowTimestamp);
                    }
                    // if request present and count is within allowed limit
                    if (node.getFrequency() < windowRequestCount) {
                        node.addRequest();
                        shouldPass.set(true);
                    }
                    return node;
                });
        return shouldPass.get();
    }

    @Override
    public void dispose() {
        if (!this.executorService.isShutdown()) {
            this.executorService.shutdownNow();
        }
    }

    @Getter
    private static class Node {
        private final long windowTimestamp;
        private int frequency;

        Node(long windowTimestamp) {
            this.windowTimestamp = windowTimestamp;
            this.frequency = 1;
        }

        void addRequest() {
            this.frequency += 1;
        }
    }
}
