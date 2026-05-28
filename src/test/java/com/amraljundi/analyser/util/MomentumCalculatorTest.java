package com.amraljundi.analyser.util;

import com.amraljundi.analyser.model.StockPrice;
import com.amraljundi.analyser.model.StockSymbol;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static com.amraljundi.analyser.model.TrendDirection.BEARISH;
import static com.amraljundi.analyser.model.TrendDirection.BULLISH;
import static com.amraljundi.analyser.model.TrendDirection.NEUTRAL;
import static com.amraljundi.analyser.util.MomentumCalculator.calculateMomentum;
import static java.math.BigDecimal.ZERO;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertThrows;

class MomentumCalculatorTest {

    private static final StockSymbol AAPL = new StockSymbol("AAPL");

    @Test
    void returns_bullish_when_price_increases() {
        // Given prices trending upward
        var prices = List.of(
                new StockPrice(AAPL, new BigDecimal("100.00"), LocalDate.of(2024, 1, 1), 1000000),
                new StockPrice(AAPL, new BigDecimal("105.00"), LocalDate.of(2024, 1, 2), 1000000),
                new StockPrice(AAPL, new BigDecimal("110.00"), LocalDate.of(2024, 1, 3), 1000000)
        );

        // When calculating momentum
        var momentum = calculateMomentum(AAPL, prices, 2);

        // Then direction is bullish
        assertEquals(BULLISH, momentum.direction());
        assertTrue(momentum.percentChange().compareTo(ZERO) > 0);
    }

    @Test
    void returns_bearish_when_price_decreases() {
        // Given prices trending downward
        var prices = List.of(
                new StockPrice(AAPL, new BigDecimal("110.00"), LocalDate.of(2024, 1, 1), 1000000),
                new StockPrice(AAPL, new BigDecimal("105.00"), LocalDate.of(2024, 1, 2), 1000000),
                new StockPrice(AAPL, new BigDecimal("100.00"), LocalDate.of(2024, 1, 3), 1000000)
        );

        // When calculating momentum
        var momentum = calculateMomentum(AAPL, prices, 2);

        // Then direction is bearish
        assertEquals(BEARISH, momentum.direction());
        assertTrue(momentum.percentChange().compareTo(ZERO) < 0);
    }

    @Test
    void returns_neutral_when_price_unchanged() {
        // Given prices with no change
        var prices = List.of(
                new StockPrice(AAPL, new BigDecimal("100.00"), LocalDate.of(2024, 1, 1), 1000000),
                new StockPrice(AAPL, new BigDecimal("100.00"), LocalDate.of(2024, 1, 2), 1000000),
                new StockPrice(AAPL, new BigDecimal("100.00"), LocalDate.of(2024, 1, 3), 1000000)
        );

        // When calculating momentum
        var momentum = calculateMomentum(AAPL, prices, 2);

        // Then direction is neutral
        assertEquals(NEUTRAL, momentum.direction());
        assertEquals(ZERO.setScale(2), momentum.percentChange());
    }

    @Test
    void throws_when_not_enough_data() {
        // Given insufficient price data
        var prices = List.of(
                new StockPrice(AAPL, new BigDecimal("100.00"), LocalDate.of(2024, 1, 1), 1000000)
        );

        // When calculating momentum with period 2
        // Then an exception is thrown
        assertThrows(IllegalArgumentException.class, () ->
                calculateMomentum(AAPL, prices, 2)
        );
    }

    @Test
    void calculates_percent_change_correctly() {
        // Given a 10% price increase
        var prices = List.of(
                new StockPrice(AAPL, new BigDecimal("100.00"), LocalDate.of(2024, 1, 1), 1000000),
                new StockPrice(AAPL, new BigDecimal("110.00"), LocalDate.of(2024, 1, 2), 1000000)
        );

        // When calculating momentum
        var momentum = calculateMomentum(AAPL, prices, 1);

        // Then percent change is 10%
        assertEquals(new BigDecimal("10.00"), momentum.percentChange());
    }
}
