package com.amraljundi.analyser.util;

import com.amraljundi.analyser.model.Momentum;
import com.amraljundi.analyser.model.StockPrice;
import com.amraljundi.analyser.model.StockSymbol;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;

import static com.amraljundi.analyser.model.TrendDirection.*;
import static java.math.BigDecimal.ZERO;
import static java.math.RoundingMode.HALF_UP;

public final class MomentumCalculator {

    public static Momentum calculateMomentum(StockSymbol symbol, List<StockPrice> prices, int period) {
        if (prices.size() < period + 1) {
            throw new IllegalArgumentException("Not enough data for symbol: " + symbol.value() + ". Need " + (period + 1) + ", got " + prices.size());
        }

        final var sorted = prices
                .stream()
                .sorted(Comparator.comparing(StockPrice::date)).toList();

        final var current = sorted.getLast();
        final var previous = sorted.get(sorted.size() - 1 - period);

        final var percentChange = calculatePercentChange(current, previous);

        final var direction = switch (percentChange.compareTo(ZERO)) {
            case 1 -> BULLISH;
            case -1 -> BEARISH;
            default -> NEUTRAL;
        };

        return new Momentum(symbol, direction, percentChange);
    }

    private static BigDecimal calculatePercentChange(StockPrice current, StockPrice previous) {
        return current.price()
                .subtract(previous.price())
                .divide(previous.price(), 4, HALF_UP)
                .multiply(BigDecimal.valueOf(100))
                .setScale(2, HALF_UP);
    }

}
