package org.example;

import java.util.Arrays;
import org.example.config.RateLimiterConfiguration;
import org.example.models.Request;
import org.example.models.RequestType;
import org.example.strategy.LimitingStrategy;

public class RateLimiter {
    private final RateLimiterConfiguration configuration;
    private final LimitingStrategy defaultStrategy;

    public RateLimiter(LimitingStrategy defaultStrategy, RateLimiterConfiguration configuration) {
        this.configuration = configuration;
        this.defaultStrategy = defaultStrategy;
    }

    public void filter(Request request) {
        LimitingStrategy limitingStrategy =
                this.configuration.getOrDefault(request.getRequestType(), defaultStrategy);
        if (limitingStrategy.filter(request)) {
            request.pass();
        } else {
            request.drop();
        }
    }

    public void shutdown() {
        this.defaultStrategy.dispose();
        Arrays.stream(RequestType.values())
                .forEach(
                        requestType -> {
                            LimitingStrategy limitingStrategy = this.configuration.get(requestType);
                            if (limitingStrategy != null) {
                                limitingStrategy.dispose();
                            }
                        });
    }
}
