package org.example;

import static java.lang.String.format;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;
import org.example.coupons.Coupon;
import org.example.coupons.impl.FlatDiscountCoupon;
import org.example.coupons.impl.PercentDiscountCoupon;
import org.example.models.Item;
import org.example.models.ItemType;
import org.example.models.User;
import org.example.models.UserType;
import org.example.terms.DiscountTerm;
import org.example.terms.impl.MinimumOrderValueDiscountTerm;
import org.example.terms.impl.UserTypeDiscountTerm;

public class Main {
    private static List<CompletableFuture<Void>> runs;

    public static void main(String[] args) {
        CouponService couponService = new CouponService();
        AtomicInteger randomUser = new AtomicInteger(1);

        // To check status/manage each thread/user run
        runs = new ArrayList<>();

        DiscountTerm minimumOrderValueDiscountTerm = new MinimumOrderValueDiscountTerm(200);
        DiscountTerm premiumUserDiscountTerm = new UserTypeDiscountTerm(UserType.PREMIUM);
        DiscountTerm vipUserDiscountTerm = new UserTypeDiscountTerm(UserType.VIP);

        // admin user adds the discount
        runUser(
                () -> {
                    final User user = new User(randomUser.getAndIncrement(), UserType.ADMIN);
                    threadPrinter(user.getUserId(), "Adding coupons");

                    // Premium Coupons
                    couponService.addCoupon(
                            new PercentDiscountCoupon(
                                    10,
                                    List.of(
                                            minimumOrderValueDiscountTerm,
                                            premiumUserDiscountTerm)),
                            1);
                    couponService.addCoupon(
                            new FlatDiscountCoupon(
                                    50,
                                    List.of(
                                            minimumOrderValueDiscountTerm,
                                            premiumUserDiscountTerm)),
                            1);

                    // VIP Coupons
                    couponService.addCoupon(
                            new PercentDiscountCoupon(
                                    5, List.of(minimumOrderValueDiscountTerm, vipUserDiscountTerm)),
                            1);
                    couponService.addCoupon(
                            new FlatDiscountCoupon(
                                    100,
                                    List.of(minimumOrderValueDiscountTerm, vipUserDiscountTerm)),
                            1);

                    threadPrinter(
                            user.getUserId(),
                            format(
                                    "Viewing all available coupons: %s",
                                    couponService.getAllCoupons()));
                });

        try {
            System.out.println("Main thread waiting for coupons to be added");
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        runUser(
                () -> {
                    final User user = new User(randomUser.getAndIncrement(), UserType.PREMIUM);

                    user.getCart().addItem(new Item("Rice", 100, ItemType.GROCERY), 2);
                    user.getCart().addItem(new Item("Mobile", 65000, ItemType.ELECTRONIC), 1);
                    user.getCart().addItem(new Item("Shirt", 1200, ItemType.FASHION), 1);

                    threadPrinter(
                            user.getUserId(),
                            format(
                                    "Viewing only applicable coupons: %s",
                                    couponService.getApplicableCoupons(user.getCart())));

                    try {
                        Coupon coupon =
                                couponService.getApplicableCoupons(user.getCart()).stream()
                                        .findAny()
                                        .orElseThrow();
                        threadPrinter(user.getUserId(), format("Trying to use coupon: %s", coupon));
                        couponService.useCoupon(coupon);
                        threadPrinter(
                                user.getUserId(), format("Successfully used coupon: %s", coupon));
                    } catch (Exception e) {
                        threadPrinter(
                                user.getUserId(),
                                format("Unable to use coupon due to error: %s", e.getMessage()));
                    }
                });

        runUser(
                () -> {
                    final User user = new User(randomUser.getAndIncrement(), UserType.VIP);

                    user.getCart().addItem(new Item("Rice", 100, ItemType.GROCERY), 2);
                    user.getCart().addItem(new Item("Mobile", 65000, ItemType.ELECTRONIC), 1);
                    user.getCart().addItem(new Item("Shirt", 1200, ItemType.FASHION), 1);

                    threadPrinter(
                            user.getUserId(),
                            format(
                                    "Viewing only applicable coupons: %s",
                                    couponService.getApplicableCoupons(user.getCart())));

                    try {
                        Coupon coupon =
                                couponService.getApplicableCoupons(user.getCart()).stream()
                                        .findAny()
                                        .orElseThrow();
                        threadPrinter(user.getUserId(), format("Trying to use coupon: %s", coupon));
                        couponService.useCoupon(coupon);
                        threadPrinter(
                                user.getUserId(), format("Successfully used coupon: %s", coupon));
                    } catch (Exception e) {
                        threadPrinter(
                                user.getUserId(),
                                format("Unable to use coupon due to error: %s", e.getMessage()));
                    }
                });

        CompletableFuture.allOf(runs.toArray(new CompletableFuture[0])).join();
        System.out.println("\n\nRemaining Coupons:");
        System.out.println(couponService.getAllCoupons());
    }

    private static void runUser(Runnable runnable) {
        CompletableFuture<Void> run = CompletableFuture.runAsync(runnable);
        runs.add(run);
    }

    // This utility part is just for better visuals for differentiating outputs from separate
    // threads
    private static final String[] colors = {
        "\u001B[0m",
        "\u001B[31m",
        "\u001B[32m",
        "\u001B[33m",
        "\u001B[34m",
        "\u001B[35m",
        "\u001B[36m"
    };

    private static void threadPrinter(Integer colorIndex, String message) {
        // colors[0] to reset color of console after printing user detail with color
        System.out.println(
                colors[colorIndex % colors.length]
                        + "[User: "
                        + colorIndex
                        + "] "
                        + colors[0]
                        + message);
    }
}
