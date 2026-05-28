package com.amraljundi.analyser.model;

import java.math.BigDecimal;
import java.time.LocalDate;

import static java.util.Objects.requireNonNull;

public record StockPrice(
        StockSymbol symbol,
        BigDecimal price,
        LocalDate date,
        long volume
) {

    public StockPrice {
        requireNonNull(symbol, "Symbol cannot be null");
        requireNonNull(date, "Date cannot be null");
        if (price == null || price.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Price must be positive");
        }
    }
}
