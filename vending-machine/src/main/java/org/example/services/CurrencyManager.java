package org.example.services;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.example.models.Currency;
import org.example.util.Locker;

public class CurrencyManager extends Locker<Currency> {
    private final ConcurrentHashMap<Currency, Integer> _inventory;
    private final ScheduledExecutorService _cleanupScheduler;
    private final AtomicLong totalAmountAvailable;

    public CurrencyManager() {
        super();
        this._inventory = new ConcurrentHashMap<>();
        this.totalAmountAvailable = new AtomicLong(0L);

        // To release locked resources at periodic intervals automatically if not released by the
        // user
        this._cleanupScheduler = Executors.newScheduledThreadPool(1);
        this._cleanupScheduler.schedule(this::autoCleanup, 30L, TimeUnit.SECONDS);
    }

    private void autoCleanup() {
        super.cleanup().forEach(this::addCurrencyToInventory);
    }

    public void addCurrencyToInventory(Currency currency, int quantity) {
        this._inventory.compute(currency, (k, v) -> (v == null ? 0 : v) + quantity);
        this.totalAmountAvailable.getAndAdd(currency.value() * quantity);
    }

    public Map<Currency, Integer> getAvailableCash() {
        return new HashMap<>(this._inventory);
    }

    @Override
    public String lock(Currency currency, Integer quantity) throws NoSuchElementException {
        StringBuilder lockIdBuilder = new StringBuilder();

        // the lambda to compute() is performed atomically, this is to enhance
        // concurrent performance rather than using synchronizing this function
        this._inventory.compute(
                currency,
                (k, availability) -> {
                    if (availability == null) {
                        throw new RuntimeException(
                                "Currency " + currency.toString() + " not found");
                    } else if (availability < quantity) {
                        throw new RuntimeException(
                                "Required quantity of Currency "
                                        + currency.toString()
                                        + " is unavailable");
                    }
                    lockIdBuilder.append(super.lock(currency, quantity));
                    this.totalAmountAvailable.getAndAdd(-1 * currency.value() * quantity);
                    return availability - quantity;
                });
        return lockIdBuilder.toString();
    }

    @Override
    public Map.Entry<Currency, Integer> rollback(String lockId) {
        Map.Entry<Currency, Integer> rollbackCurrency = super.rollback(lockId);
        if (rollbackCurrency == null) return null;
        addCurrencyToInventory(rollbackCurrency.getKey(), rollbackCurrency.getValue());
        return rollbackCurrency;
    }

    public long getTotalAmountAvailable() {
        return this.totalAmountAvailable.get();
    }

    // TODO: call combination sum on sorted map to select notes of different type. If sum can't be
    // created throw error
    public static Map<Currency, Long> getChangeCurrencyMap(
            int amount, Map<Currency, Integer> currencyMap) throws RuntimeException {
        if (amount <= 0) {
            throw new RuntimeException("Amount should be a positive value");
        }
        List<Currency> currencyList = new ArrayList<>();
        // call combination sum on sorted map to select notes of different type
        // if sum can't be created throw error
        return currencyList.stream()
                .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()));
    }

    public void shutdown() {
        this._cleanupScheduler.shutdown();
    }
}
