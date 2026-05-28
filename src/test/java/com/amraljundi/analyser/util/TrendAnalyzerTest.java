package com.amraljundi.analyser.util;

import com.amraljundi.analyser.model.Momentum;
import com.amraljundi.analyser.model.StockSymbol;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import static com.amraljundi.analyser.model.TrendDirection.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;


class TrendAnalyzerTest {

    private static final StockSymbol AAPL = new StockSymbol("AAPL");
    private static final StockSymbol GOOGL = new StockSymbol("GOOGL");
    private static final StockSymbol MSFT = new StockSymbol("MSFT");
    private static final StockSymbol AMZN = new StockSymbol("AMZN");
    private static final StockSymbol META = new StockSymbol("META");

    @Test
    void returns_bullish_when_70_percent_or_more_stocks_are_bullish() {
        // Given 4 out of 5 stocks are bullish
        var momentums = Map.of(
                AAPL, new Momentum(AAPL, BULLISH, new BigDecimal("3.00")),
                GOOGL, new Momentum(GOOGL, BULLISH, new BigDecimal("2.00")),
                MSFT, new Momentum(MSFT, BULLISH, new BigDecimal("1.00")),
                AMZN, new Momentum(AMZN, BULLISH, new BigDecimal("1.50")),
                META, new Momentum(META, BEARISH, new BigDecimal("-1.00"))
        );

        // When analyzing sector trend
        var report = TrendAnalyzer.analyze(momentums, List.of());

        // Then sector trend is bullish
        assertEquals(BULLISH, report.sectorTrend());
        assertEquals(0.80, report.confidence());
    }

    @Test
    void returns_bearish_when_30_percent_or_less_stocks_are_bullish() {
        // Given 1 out of 5 stocks is bullish
        var momentums = Map.of(
                AAPL, new Momentum(AAPL, BEARISH, new BigDecimal("-3.00")),
                GOOGL, new Momentum(GOOGL, BEARISH, new BigDecimal("-2.00")),
                MSFT, new Momentum(MSFT, BEARISH, new BigDecimal("-1.00")),
                AMZN, new Momentum(AMZN, BEARISH, new BigDecimal("-1.50")),
                META, new Momentum(META, BULLISH, new BigDecimal("1.00"))
        );

        // When analyzing sector trend
        var report = TrendAnalyzer.analyze(momentums, List.of());

        // Then sector trend is bearish
        assertEquals(BEARISH, report.sectorTrend());
        assertEquals(0.80, report.confidence());
    }

    @Test
    void returns_neutral_when_mixed_signals() {
        // Given 3 out of 5 stocks are bullish
        var momentums = Map.of(
                AAPL, new Momentum(AAPL, BULLISH, new BigDecimal("3.00")),
                GOOGL, new Momentum(GOOGL, BULLISH, new BigDecimal("2.00")),
                MSFT, new Momentum(MSFT, BULLISH, new BigDecimal("1.00")),
                AMZN, new Momentum(AMZN, BEARISH, new BigDecimal("-1.50")),
                META, new Momentum(META, BEARISH, new BigDecimal("-1.00"))
        );

        // When analyzing sector trend
        var report = TrendAnalyzer.analyze(momentums, List.of());

        // Then sector trend is neutral
        assertEquals(NEUTRAL, report.sectorTrend());
    }

    @Test
    void confidence_reflects_agreement_among_stocks() {
        // Given all stocks bullish
        var momentums = Map.of(
                AAPL, new Momentum(AAPL, BULLISH, new BigDecimal("3.00")),
                GOOGL, new Momentum(GOOGL, BULLISH, new BigDecimal("2.00")),
                MSFT, new Momentum(MSFT, BULLISH, new BigDecimal("1.00")),
                AMZN, new Momentum(AMZN, BULLISH, new BigDecimal("1.50")),
                META, new Momentum(META, BULLISH, new BigDecimal("2.50"))
        );

        // When analyzing sector trend
        var report = TrendAnalyzer.analyze(momentums, List.of());

        // Then confidence is 100%
        assertEquals(1.0, report.confidence());
    }

    @Test
    void report_contains_all_stock_momentums() {
        // Given two stocks
        var momentums = Map.of(
                AAPL, new Momentum(AAPL, BULLISH, new BigDecimal("3.00")),
                GOOGL, new Momentum(GOOGL, BEARISH, new BigDecimal("-2.00"))
        );

        // When analyzing sector trend
        var report = TrendAnalyzer.analyze(momentums, List.of());

        // Then report contains all stock momentums
        assertEquals(2, report.stockMomentums().size());
        assertTrue(report.stockMomentums().containsKey(AAPL));
        assertTrue(report.stockMomentums().containsKey(GOOGL));
    }

    @Test
    void report_contains_failed_symbols() {
        // Given one successful and one failed symbol
        var momentums = Map.of(
                AAPL, new Momentum(AAPL, BULLISH, new BigDecimal("3.00"))
        );
        var failedSymbols = List.of(GOOGL);

        // When analyzing sector trend
        var report = TrendAnalyzer.analyze(momentums, failedSymbols);

        // Then failed symbols are included in the report
        assertEquals(1, report.failedSymbols().size());
        assertTrue(report.failedSymbols().contains(GOOGL));
    }
}
