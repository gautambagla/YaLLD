package org.example.services;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.example.models.Currency;
import org.example.models.Order;
import org.example.util.Locker;

public class OrderManager {
    public OrderManager(
            InventoryManager inventoryManager,
            CurrencyManager currencyManager,
            Dispenser dispenser) {
        this._inventoryManager = inventoryManager;
        this._currencyManager = currencyManager;
        this._dispenser = dispenser;
    }

    private static OrderManager _orderManager;
    private final InventoryManager _inventoryManager;
    private final CurrencyManager _currencyManager;
    private final Dispenser _dispenser;

    // This method need not by synchronized as it acts as an orchestrator to 2PC
    // This also ensures if multiple users are placing orders concurrently, the orders
    // are placed with enhanced concurrency
    public void placeOrder(Order order) {
        validateOrder(order);
        int change = order.amountPaid() - order.item().price() * order.quantity();
        List<String> currencyLocks = new ArrayList<>();
        List<String> itemLocks = new ArrayList<>();

        // Implement 2-Phase Commit
        try {
            if (change > 0) {
                Map<Currency, Long> changeCurrencyMap =
                        CurrencyManager.getChangeCurrencyMap(
                                change, _currencyManager.getAvailableCash());
                changeCurrencyMap.forEach(
                        (currency, quantity) -> {
                            currencyLocks.add(_currencyManager.lock(currency, quantity.intValue()));
                        });
            }
            // if multiple items can be added, this can be handled similar to currencyManager
            itemLocks.add(_inventoryManager.lock(order.item(), order.quantity()));

            // If flow reaches here, the locks are acquired and item can be dispensed
            // Not passing order here directly to handle multiple item dispatches if placed in a
            // single
            // order in future update
            _dispenser.dispense(order.orderId(), order.item(), order.quantity());

            // if dispense fails above, the locks will be rolled back
            commitLocks(currencyLocks, _currencyManager);
            commitLocks(itemLocks, _inventoryManager);
        } catch (Exception e) {
            System.out.printf("\nCould not place order: %s", order.orderId());
            rollbackLocks(currencyLocks, _currencyManager);
            rollbackLocks(itemLocks, _inventoryManager);
        }
        // Not implementing finally here
    }

    private void rollbackLocks(List<String> locks, Locker<?> locker) {
        locks.forEach(locker::rollback);
    }

    private void commitLocks(List<String> locks, Locker<?> locker) {
        locks.forEach(locker::commit);
    }

    private void validateOrder(Order order) {
        int totalPrice = order.item().price() * order.quantity();
        int change = order.amountPaid() - totalPrice;
        if (change < 0) {
            throw new RuntimeException("Please provide the full amount and retry");
        } else if (_currencyManager.getTotalAmountAvailable() < change) {
            throw new RuntimeException("Change not available");
        }
    }
}
