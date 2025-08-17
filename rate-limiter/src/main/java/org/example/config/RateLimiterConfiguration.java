package org.example.config;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.example.models.RequestType;
import org.example.strategy.LimitingStrategy;

public class RateLimiterConfiguration {
    private final Map<RequestType, LimitingStrategy> limitingStrategies;

    public RateLimiterConfiguration() {
        limitingStrategies = new ConcurrentHashMap<>();
    }

    public void set(RequestType requestType, LimitingStrategy limitingStrategy) {
        limitingStrategies.put(requestType, limitingStrategy);
    }

    public LimitingStrategy get(RequestType requestType) {
        return limitingStrategies.get(requestType);
    }

    public LimitingStrategy getOrDefault(
            RequestType requestType, LimitingStrategy limitingStrategy) {
        return limitingStrategies.getOrDefault(requestType, limitingStrategy);
    }
}
