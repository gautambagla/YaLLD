package org.example.coupons.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.example.coupons.Coupon;
import org.example.models.Cart;
import org.example.models.Item;
import org.example.models.ItemType;
import org.example.terms.DiscountTerm;
import org.example.terms.impl.ItemTypeDiscountTerm;

@EqualsAndHashCode(callSuper = true)
@ToString
public class ItemTypeDiscountCoupon extends Coupon {
    public ItemTypeDiscountCoupon(
            Coupon coupon, ItemType itemType, List<DiscountTerm> otherDiscountTerms) {
        super(
                new ArrayList<>() {
                    {
                        addAll(otherDiscountTerms);
                        add(new ItemTypeDiscountTerm(itemType));
                    }
                });
        this.coupon = coupon;
        this.itemType = itemType;
    }

    private final Coupon coupon;
    private final ItemType itemType;

    @Override
    public Integer apply(Cart cart) {
        if (this.isApplicable(cart)) {
            // Set discount only on applicable items
            Cart clonedCart = cart.copy();
            clonedCart.clear();

            // Sum of all the items which are of itemType and need to be discounted
            Integer discountedItemsOriginalPriceSum = 0;
            for (Map.Entry<Item, Integer> originalItem : cart.getItems().entrySet()) {
                if (originalItem.getKey().itemType().equals(this.itemType)) {
                    discountedItemsOriginalPriceSum +=
                            originalItem.getKey().price() * originalItem.getValue();
                } else {
                    clonedCart.removeItem(originalItem.getKey(), originalItem.getValue());
                }
            }

            return cart.getTotalValue()
                    - discountedItemsOriginalPriceSum
                    + coupon.apply(clonedCart);
        } else {
            throw new RuntimeException(
                    "ItemTypeDiscountCoupon Invalid for Cart: " + cart.toString());
        }
    }
}
