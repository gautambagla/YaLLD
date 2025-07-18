package org.example.services;

import org.example.models.Item;

public class Dispenser {
    private static Dispenser _dispenser;

    private Dispenser() {}

    public static Dispenser getInstance() {
        if (_dispenser == null) _dispenser = new Dispenser();
        return _dispenser;
    }

    public void dispense(String orderId, Item item, Integer quantity) {
        System.out.printf(
                "\nOrderID: [%s] Dispensing %s quantity of %s%n",
                orderId, quantity, item.toString());
    }
}
