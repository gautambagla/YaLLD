package org.example.models;

import java.util.Date;
import java.util.UUID;
import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
public class Request {
    private final String requestId;
    private final String userId;
    private final String ipAddress;
    private final RequestType requestType;
    private final long timestamp;

    private Request(String userId, String ipAddress, RequestType requestType) {
        this.requestId = UUID.randomUUID().toString();
        this.userId = userId;
        if (ipAddress == null || ipAddress.isEmpty()) {
            throw new RuntimeException("Invalid Source IP Address");
        }
        this.ipAddress = ipAddress;
        this.requestType = requestType;
        this.timestamp = new Date().getTime();
    }

    public void pass() {
        System.out.printf("%s succeeded!\n", this);
    }

    public void drop() {
        System.out.printf("%s dropped!\n", this);
    }

    // Just utility methods, not part of UML/ER
    public static Request createAnonymousRequest(String ipAddress) {
        return new Request(null, ipAddress, RequestType.SEARCH_ANONYMOUS);
    }

    public static Request createRegisteredRequest(String userId, String ipAddress) {
        if (userId == null || userId.isEmpty()) {
            throw new RuntimeException("User ID Required for Registered Request");
        }
        return new Request(userId, ipAddress, RequestType.SEARCH_REGISTERED);
    }

    public static Request createLoginRequest(String userId, String ipAddress) {
        if (userId == null || userId.isEmpty()) {
            throw new RuntimeException("User ID Required for Registered Request");
        }
        return new Request(userId, ipAddress, RequestType.LOGIN);
    }
}
