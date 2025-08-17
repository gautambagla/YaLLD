package org.example.strategy;

import org.example.models.LimitingKey;
import org.example.models.Request;

public interface LimitingStrategy {
    boolean filter(Request request);

    void dispose();

    LimitingKey getLimitingKey();

    default String getKey(Request request) {
        switch (getLimitingKey()) {
            case USER_ID -> {
                return request.getUserId();
            }
            case IP_ADDRESS -> {
                return request.getIpAddress();
            }
        }
        return request.getIpAddress();
    }
}
