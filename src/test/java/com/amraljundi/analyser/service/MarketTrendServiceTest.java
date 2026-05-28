package com.amraljundi.analyser.service;

import com.amraljundi.analyser.model.StockPrice;
import com.amraljundi.analyser.model.StockSymbol;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static com.amraljundi.analyser.model.TrendDirection.BULLISH;
import static com.amraljundi.analyser.model.TrendDirection.NEUTRAL;
import static org.junit.jupiter.api.Assertions.assertEquals;

class MarketTrendServiceTest {

    private static final StockSymbol AAPL = new StockSymbol("AAPL");
    private static final StockSymbol GOOGL = new StockSymbol("GOOGL");
    private static final StockSymbol MSFT = new StockSymbol("MSFT");

    private MarketTrendService service;

    @BeforeEach
    void setUp() {
        var stub = new StockApiServiceStub(Map.of(
                AAPL, buildPrices(AAPL, "100.00", "110.00"),
                GOOGL, buildPrices(GOOGL, "100.00", "105.00"),
                MSFT, buildPrices(MSFT, "100.00", "108.00")
        ));
        service = new MarketTrendService(stub);
    }

    @Test
    void returns_report_with_all_analyzed_symbols() {
        // Given three valid symbols
        var symbols = List.of(AAPL, GOOGL, MSFT);

        // When analyzing sector trend
        var report = service.analyze(symbols, 1);

        // Then report contains all symbols
        assertEquals(3, report.stockMomentums().size());
    }

    @Test
    void returns_bullish_when_all_stocks_trending_up() {
        // Given two bullish and one bearish stock
        var symbols = List.of(AAPL, GOOGL, MSFT);

        // When analyzing sector trend
        var report = service.analyze(symbols, 1);

        // Then sector trend is bullish
        assertEquals(BULLISH, report.sectorTrend());
    }

    @Test
    void returns_neutral_when_below_bullish_threshold() {
        // Given two out of three stocks bullish (66% - below 70% threshold)
        var stub = new StockApiServiceStub(Map.of(
                AAPL, buildPrices(AAPL, "100.00", "110.00"),
                GOOGL, buildPrices(GOOGL, "100.00", "105.00"),
                MSFT, buildPrices(MSFT, "110.00", "100.00")
        ));
        var localService = new MarketTrendService(stub);

        // When analyzing sector trend
        var report = localService.analyze(List.of(AAPL, GOOGL, MSFT), 1);

        // Then sector trend is neutral not bullish
        assertEquals(NEUTRAL, report.sectorTrend());
    }

    @Test
    void returns_partial_report_when_some_symbols_fail() {
        // Given one valid and one unknown symbol
        var symbols = List.of(AAPL, new StockSymbol("UNKNOWN"));

        // When analyzing sector trend
        var report = service.analyze(symbols, 1);

        // Then report contains only successful symbol
        assertEquals(1, report.stockMomentums().size());
    }

    private List<StockPrice> buildPrices(StockSymbol symbol, String startPrice, String endPrice) {
        return List.of(
                new StockPrice(symbol, new BigDecimal(startPrice), LocalDate.of(2024, 1, 1), 1000000),
                new StockPrice(symbol, new BigDecimal(endPrice), LocalDate.of(2024, 1, 2), 1000000)
        );
    }
}
