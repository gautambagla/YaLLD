package org.example.models;

import java.util.UUID;

public record Order(String orderId, Item item, Integer quantity, Integer amountPaid) {

    public Order(Item item, Integer quantity, Integer amountPaid) {
        this(UUID.randomUUID().toString(), item, quantity, amountPaid);
    }

    public Order {
        orderId = UUID.randomUUID().toString();
    }
}
