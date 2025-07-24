package org.example.terms.impl;

import lombok.EqualsAndHashCode;
import org.example.models.Cart;
import org.example.terms.DiscountTerm;

@EqualsAndHashCode
public class MinimumOrderValueDiscountTerm implements DiscountTerm {
    public MinimumOrderValueDiscountTerm(Integer minimumOrderValue) {
        this.minimumOrderValue = minimumOrderValue;
    }

    private final Integer minimumOrderValue;

    @Override
    public boolean isApplicable(Cart cart) {
        return cart.getTotalValue() >= this.minimumOrderValue;
    }
}
