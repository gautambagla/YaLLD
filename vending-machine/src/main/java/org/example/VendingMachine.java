package org.example;

import org.example.models.Item;
import org.example.models.Order;
import org.example.services.CurrencyManager;
import org.example.services.InventoryManager;
import org.example.services.OrderManager;

public class VendingMachine {
    private final OrderManager _orderManager;
    private final InventoryManager _inventoryManager;
    private static VendingMachine _vendingMachine;

    // Vending Machine is Singleton because inventoryManager, orderManager are singleton
    private VendingMachine() {
        this._orderManager = OrderManager.getInstance();
        this._inventoryManager = InventoryManager.getInstance();
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
            throw new RuntimeException("Quantity should be a positive value");
        }
        _inventoryManager.addItemToInventory(item, quantity);
    }

    public String purchaseItem(Item item, Integer quantity, Integer amountPaid) {
        Order order = new Order(item, quantity, amountPaid);
        this._orderManager.placeOrder(order);
        return order.orderId();
    }

    // To be called after all threads (users) have completed execution
    public void shutdown() {
        System.out.println("\n\nAttempting to shutdown Vending Machine...");
        InventoryManager.getInstance().shutdown();
        CurrencyManager.getInstance().shutdown();
    }
}
