package org.example.coupons.impl;

import java.util.List;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.example.coupons.Coupon;
import org.example.models.Cart;
import org.example.terms.DiscountTerm;

@EqualsAndHashCode(callSuper = true)
@ToString
public class FlatDiscountCoupon extends Coupon {
    public FlatDiscountCoupon(Integer discount, List<DiscountTerm> discountTerms) {
        super(discountTerms);
        this.discount = discount;
    }

    private final Integer discount;

    @Override
    public Integer apply(Cart cart) {
        if (this.isApplicable(cart)) {
            return cart.getTotalValue() - this.discount;
        } else {
            throw new RuntimeException("FlatDiscountCoupon Invalid for Cart: " + cart.toString());
        }
    }
}
