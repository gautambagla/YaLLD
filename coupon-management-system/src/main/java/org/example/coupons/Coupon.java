package org.example.coupons;

import java.util.ArrayList;
import java.util.List;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.example.models.Cart;
import org.example.terms.DiscountTerm;

@EqualsAndHashCode
@ToString
public abstract class Coupon {
    protected Coupon(List<DiscountTerm> discountTerms) {
        this.discountTerms = new ArrayList<>(discountTerms);
    }

    private final List<DiscountTerm> discountTerms;

    public boolean isApplicable(Cart cart) {
        for (DiscountTerm discountTerm : this.discountTerms) {
            if (!discountTerm.isApplicable(cart)) {
                return false;
            }
        }
        return true;
    }

    public abstract Integer apply(Cart cart);
}
