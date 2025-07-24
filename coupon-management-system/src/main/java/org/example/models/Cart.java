package org.example.models;

import java.util.HashMap;
import java.util.Map;
import lombok.Getter;
import lombok.ToString;

@ToString
public class Cart {
    public Cart(User user) {
        this.user = user;
        this.items = new HashMap<>();
    }

    private Cart(Cart cart) {
        this.user = cart.getUser();
        this.items = cart.getItems();
    }

    @Getter private final User user;
    private final Map<Item, Integer> items;

    public Map<Item, Integer> getItems() {
        return new HashMap<>(this.items);
    }

    public void addItem(Item item, Integer quantity) {
        this.items.compute(item, (it, freq) -> ((freq == null) ? 0 : freq) + quantity);
    }

    public void removeItem(Item item, Integer quantity) {
        Integer existingQty = this.items.get(item);
        if (existingQty == null) return;
        if (existingQty <= quantity) this.items.remove(item);
        else this.items.put(item, existingQty - quantity);
    }

    public void clear() {
        this.items.clear();
    }

    public Integer getTotalValue() {
        return this.items.entrySet().stream()
                .mapToInt(entry -> entry.getKey().price() * entry.getValue())
                .sum();
    }

    public Cart copy() {
        return new Cart(this);
    }
}
