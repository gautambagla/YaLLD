package org.example.coupons.impl;

import java.util.List;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.example.coupons.Coupon;
import org.example.models.Cart;
import org.example.terms.DiscountTerm;

@EqualsAndHashCode(callSuper = true)
@ToString
public class PercentDiscountCoupon extends Coupon {
    public PercentDiscountCoupon(Integer discountRate, List<DiscountTerm> discountTerms) {
        super(discountTerms);
        if (discountRate > 100) {
            throw new IllegalArgumentException("Discount Rate cannot exceed 100");
        }
        this.discountRate = discountRate;
    }

    private final Integer discountRate;

    @Override
    public Integer apply(Cart cart) {
        if (this.isApplicable(cart)) {
            return cart.getTotalValue() - cart.getTotalValue() * discountRate / 100;
        } else {
            throw new RuntimeException(
                    "PercentDiscount Coupon Invalid for Cart: " + cart.toString());
        }
    }
}
