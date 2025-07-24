package org.example.terms.impl;

import org.example.models.Cart;
import org.example.models.ItemType;
import org.example.terms.DiscountTerm;

public class ItemTypeDiscountTerm implements DiscountTerm {
    public ItemTypeDiscountTerm(ItemType itemType) {
        this.itemType = itemType;
    }

    private final ItemType itemType;

    @Override
    public boolean isApplicable(Cart cart) {
        // If any of the items in the cart is of this itemType, then the term is satisfied
        return cart.getItems().keySet().stream()
                .anyMatch(item -> item.itemType().equals(this.itemType));
    }
}
