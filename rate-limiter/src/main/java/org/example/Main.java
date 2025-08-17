package org.example;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;
import org.example.config.RateLimiterConfiguration;
import org.example.models.LimitingKey;
import org.example.models.Request;
import org.example.models.RequestType;
import org.example.strategy.impl.FixedWindowCounterStrategy;
import org.example.strategy.impl.SlidingWindowLogStrategy;

public class Main {
    private static List<CompletableFuture<Void>> runs;

    public static void main(String[] args) {
        RateLimiterConfiguration rateLimiterConfiguration = new RateLimiterConfiguration();
        rateLimiterConfiguration.set(
                RequestType.SEARCH_ANONYMOUS,
                new SlidingWindowLogStrategy(LimitingKey.IP_ADDRESS, 2000, 5));
        rateLimiterConfiguration.set(
                RequestType.SEARCH_REGISTERED,
                new SlidingWindowLogStrategy(LimitingKey.USER_ID, 2000, 10));
        rateLimiterConfiguration.set(
                RequestType.LOGIN, new FixedWindowCounterStrategy(LimitingKey.USER_ID, 2000, 5));

        RateLimiter rateLimiter =
                new RateLimiter(
                        new FixedWindowCounterStrategy(LimitingKey.IP_ADDRESS, 5000, 5),
                        rateLimiterConfiguration);

        AtomicInteger randomUser = new AtomicInteger(1);
        // To check status/manage each thread/user run
        runs = new ArrayList<>();

        runUser(
                () -> {
                    final ThreadLocal<Integer> user =
                            ThreadLocal.withInitial(randomUser::getAndIncrement);
                    for (int i = 1; i <= 50; ++i) {
                        rateLimiter.filter(Request.createAnonymousRequest("0.0.0.0"));
                        try {
                            Thread.sleep(100);
                        } catch (InterruptedException ignored) {
                        }
                    }
                });

        runUser(
                () -> {
                    final ThreadLocal<Integer> user =
                            ThreadLocal.withInitial(randomUser::getAndIncrement);
                    for (int i = 1; i <= 50; ++i) {
                        rateLimiter.filter(
                                Request.createLoginRequest(user.get().toString(), "1.2.3.4"));
                        try {
                            Thread.sleep(100);
                        } catch (InterruptedException ignored) {
                        }
                    }
                });

        runUser(
                () -> {
                    final ThreadLocal<Integer> user =
                            ThreadLocal.withInitial(randomUser::getAndIncrement);
                    for (int i = 1; i <= 50; ++i) {
                        rateLimiter.filter(
                                Request.createRegisteredRequest(
                                        user.get().toString(), "255.255.255.255"));
                        try {
                            Thread.sleep(100);
                        } catch (InterruptedException ignored) {
                        }
                    }
                });

        CompletableFuture.allOf(runs.toArray(new CompletableFuture[0]))
                .thenRun(rateLimiter::shutdown);
    }

    private static void runUser(Runnable runnable) {
        CompletableFuture<Void> run = CompletableFuture.runAsync(runnable);
        runs.add(run);
    }
}
