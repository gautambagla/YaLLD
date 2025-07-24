package org.example;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.example.coupons.Coupon;
import org.example.models.Cart;

public class CouponService {
    private final ConcurrentHashMap<Coupon, Integer> coupons;

    CouponService() {
        this.coupons = new ConcurrentHashMap<>();
    }

    public void addCoupon(Coupon coupon, Integer quantity) {
        coupons.compute(coupon, (c, q) -> (q == null ? 0 : q) + quantity);
    }

    public List<Coupon> getAllCoupons() {
        return coupons.entrySet().stream()
                .filter(couponEntry -> couponEntry.getValue() > 0)
                .map(Map.Entry::getKey)
                .toList();
    }

    public List<Coupon> getApplicableCoupons(Cart cart) {
        return coupons.entrySet().stream()
                .filter(
                        couponEntry ->
                                couponEntry.getValue() > 0
                                        && couponEntry.getKey().isApplicable(cart))
                .map(Map.Entry::getKey)
                .toList();
    }

    public void useCoupon(Coupon coupon) throws RuntimeException {
        this.coupons.compute(
                coupon,
                (c, q) -> {
                    if (q == null || q == 0) {
                        throw new RuntimeException(
                                "Coupon Usage Limit Exceeded: " + coupon.toString());
                    }
                    return q - 1;
                });
    }

    public void removeCoupon(Coupon coupon, Integer quantity) {
        coupons.computeIfPresent(coupon, (c, q) -> Math.max(q - quantity, 0));
    }

    public void removeCoupon(Coupon coupon) {
        coupons.remove(coupon);
    }
}
