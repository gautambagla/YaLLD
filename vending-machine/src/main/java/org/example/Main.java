package org.example;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;
import org.example.models.Currency;
import org.example.models.Item;

public class Main {

    private static List<CompletableFuture<Void>> runs;

    // A basic driver function
    public static void main(String[] args) {
        VendingMachine vendingMachine = VendingMachine.getInstance();
        AtomicInteger randomUser = new AtomicInteger(1);

        // To check status/manage each thread/user run
        runs = new ArrayList<>();

        // supposedly admin user to add items/currency
        runUser(
                () -> {
                    final ThreadLocal<Integer> user =
                            ThreadLocal.withInitial(randomUser::getAndIncrement);
                    System.out.printf("\nUser %s is adding items", user.get());
                    vendingMachine.addItem(new Item("Lays", 20), 5);
                    vendingMachine.addItem(new Item("Bingo", 10), 2);
                    vendingMachine.addItem(new Item("Coke", 40), 3);

                    System.out.printf("\nUser %s is adding currency", user.get());
                    vendingMachine.addCurrency(Currency.HUNDRED, 50);
                    vendingMachine.addCurrency(Currency.TEN, 50);
                    vendingMachine.addCurrency(Currency.FIVE, 50);
                    vendingMachine.addCurrency(Currency.ONE, 50);
                });

        try {
            System.out.print("\nMain thread waiting for items to be added");
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        // buyer
        runUser(
                () -> {
                    final ThreadLocal<Integer> user =
                            ThreadLocal.withInitial(randomUser::getAndIncrement);
                    System.out.printf("\nUser %s is listing items asynchronously", user.get());
                    vendingMachine.showAvailableItems();
                });

        // buyer
        runUser(
                () -> {
                    final ThreadLocal<Integer> user =
                            ThreadLocal.withInitial(randomUser::getAndIncrement);
                    // Item name, price is actually not what the customer places an order with, but
                    // an itemId
                    // Since the existing record is uniquely identifiable only by name and price as
                    // candidate
                    // key this will work only if the item exists with same name and price.
                    String orderId = vendingMachine.purchaseItem(new Item("Lays", 20), 4, 80);
                    System.out.printf(
                            "\nUser %s tried to purchase items asynchronously with orderId: %s",
                            user.get(), orderId);
                });

        // buyer
        runUser(
                () -> {
                    final ThreadLocal<Integer> user =
                            ThreadLocal.withInitial(randomUser::getAndIncrement);
                    String orderId = vendingMachine.purchaseItem(new Item("Lays", 20), 4, 80);
                    System.out.printf(
                            "\nUser %s tried to purchase items asynchronously with orderId: %s",
                            user.get(), orderId);
                });

        // buyer
        runUser(
                () -> {
                    try {
                        Thread.sleep(2000);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                    final ThreadLocal<Integer> user =
                            ThreadLocal.withInitial(randomUser::getAndIncrement);
                    System.out.printf("\nUser %s is listing items asynchronously", user.get());
                    vendingMachine.showAvailableItems();
                });

        CompletableFuture.allOf(runs.toArray(new CompletableFuture[0]))
                .thenRun(vendingMachine::shutdown);
    }

    private static void runUser(Runnable runnable) {
        CompletableFuture<Void> run = CompletableFuture.runAsync(runnable);
        runs.add(run);
    }
}
