package org.example.models;

public enum Currency {
    HUNDRED(100L),
    TEN(10L),
    FIVE(5L),
    ONE(1L);

    private final Long value;

    Currency(Long value) {
        this.value = value;
    }

    public Long value() {
        return this.value;
    }
}
