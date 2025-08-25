package org.example.services;

import org.example.models.Item;

public class Dispenser {
    public void dispense(String orderId, Item item, Integer quantity) {
        System.out.printf(
                "\nOrderID: [%s] Dispensing %s quantity of %s%n",
                orderId, quantity, item.toString());
    }
}
