package org.example;

import org.example.models.Item;

import java.util.concurrent.atomic.AtomicInteger;

public class Main {
    //A basic driver function
    public static void main(String[] args) {
        VendingMachine vendingMachine = VendingMachine.getInstance();
        AtomicInteger randomUser = new AtomicInteger(1);

        // supposedly admin user to add items
        runUser(
                () -> {
                    final ThreadLocal<Integer> user = ThreadLocal.withInitial(randomUser::getAndIncrement);
                    System.out.printf("\nUser %s is adding items asynchronously", user.get());
                    vendingMachine.addItem(new Item("Lays", 20), 5);
                    vendingMachine.addItem(new Item("Bingo", 10), 2);
                    vendingMachine.addItem(new Item("Coke", 40), 3);
                });

        try {
            System.out.print("\nMain thread waiting for items to be added");
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        // buyer
        runUser(() -> {
            final ThreadLocal<Integer> user = ThreadLocal.withInitial(randomUser::getAndIncrement);
            System.out.printf("\nUser %s is listing items asynchronously", user.get());
            vendingMachine.showAvailableItems();
        });

        // buyer
        runUser(
                () -> {
                    final ThreadLocal<Integer> user = ThreadLocal.withInitial(randomUser::getAndIncrement);
                    // Item name, price is actually not what the customer places an order with, but an itemId
                    // Since the existing record is uniquely identifiable only by name and price as candidate
                    // key this will work only if the item exists with same name and price.
                    String orderId = vendingMachine.purchaseItem(new Item("Lays", 20), 4, 80);
                    System.out.printf("\nUser %s tried to purchase items asynchronously with orderId: %s", user.get(), orderId);
                });

        // buyer
        runUser(
                () -> {
                    final ThreadLocal<Integer> user = ThreadLocal.withInitial(randomUser::getAndIncrement);
                    String orderId = vendingMachine.purchaseItem(new Item("Lays", 20), 4, 80);
                    System.out.printf("\nUser %s tried to purchase items asynchronously with orderId: %s", user.get(), orderId);
                });

        // buyer
        runUser(() -> {
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            final ThreadLocal<Integer> user = ThreadLocal.withInitial(randomUser::getAndIncrement);
            System.out.printf("\nUser %s is listing items asynchronously", user.get());
            vendingMachine.showAvailableItems();
        });
    }

    private static void runUser(Runnable runnable) {
        new Thread(runnable).start();
    }
}
