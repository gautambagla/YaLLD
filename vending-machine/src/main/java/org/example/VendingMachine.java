package org.example;

import org.example.models.Currency;
import org.example.models.Item;
import org.example.models.Order;
import org.example.services.CurrencyManager;
import org.example.services.InventoryManager;
import org.example.services.OrderManager;

public class VendingMachine {
    private final OrderManager _orderManager;
    private final InventoryManager _inventoryManager;
    private final CurrencyManager _currencyManager;
    private static VendingMachine _vendingMachine;

    // Vending Machine is Singleton because inventoryManager, orderManager are singleton
    private VendingMachine() {
        this._orderManager = OrderManager.getInstance();
        this._inventoryManager = InventoryManager.getInstance();
        this._currencyManager = CurrencyManager.getInstance();
    }

    public static VendingMachine getInstance() {
        if (_vendingMachine == null) _vendingMachine = new VendingMachine();
        return _vendingMachine;
    }

    public void showAvailableItems() {
        this._inventoryManager
                .getAllItemsInInventory()
                .forEach((item, quantity) -> System.out.printf("\n%s: %s", item, quantity));
    }

    public void addItem(Item item, Integer quantity) {
        if (quantity <= 0) {
            throw new RuntimeException("Item quantity should be a positive value");
        }
        _inventoryManager.addItemToInventory(item, quantity);
    }

    public String purchaseItem(Item item, Integer quantity, Integer amountPaid) {
        Order order = new Order(item, quantity, amountPaid);
        this._orderManager.placeOrder(order);
        return order.orderId();
    }

    public void addCurrency(Currency currency, Integer quantity) {
        if (quantity <= 0) {
            throw new RuntimeException("Currency quantity should be a positive value");
        }
        _currencyManager.addCurrencyToInventory(currency, quantity);
    }

    // To be called after all threads (users) have completed execution
    public void shutdown() {
        System.out.println("\n\nAttempting to shutdown Vending Machine...");
        this._inventoryManager.shutdown();
        this._currencyManager.shutdown();
    }
}
