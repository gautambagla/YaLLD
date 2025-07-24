package org.example.terms;

import org.example.models.Cart;

public interface DiscountTerm {
    boolean isApplicable(Cart cart);
}
