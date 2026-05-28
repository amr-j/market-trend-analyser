package com.amraljundi.analyser.service;

import com.amraljundi.analyser.client.StockDataClientStub;
import com.amraljundi.analyser.model.StockPrice;
import com.amraljundi.analyser.model.StockSymbol;
import com.amraljundi.analyser.model.TimeSeriesResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executor;

import static java.util.concurrent.Executors.newVirtualThreadPerTaskExecutor;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class StockApiServiceImplTest {

    private static final StockSymbol AAPL = new StockSymbol("AAPL");
    private static final StockSymbol GOOGL = new StockSymbol("GOOGL");

    private StockApiServiceImpl service;

    @BeforeEach
    void setUp() {
        // virtual thread executor to test concurrent fetching
        var executor = newVirtualThreadPerTaskExecutor();
        var stub = new StockDataClientStub(Map.of(
                "AAPL", buildTimeSeriesResponse("AAPL", Map.of(
                        "2024-01-01", "180.00",
                        "2024-01-02", "182.00",
                        "2024-01-03", "184.00"
                )),
                "GOOGL", buildTimeSeriesResponse("GOOGL", Map.of(
                        "2024-01-01", "140.00",
                        "2024-01-02", "142.00",
                        "2024-01-03", "144.00"
                ))
        ));
        service = new StockApiServiceImpl(executor, stub);
    }

    @Test
    void returns_prices_for_all_symbols_when_all_succeed() {
        // Given two valid symbols
        var symbols = List.of(AAPL, GOOGL);

        // When fetching historical prices
        var result = service.fetchHistoricalPricesForSymbols(symbols, 3);

        // Then both symbols are returned with correct data
        assertEquals(2, result.prices().size());
        assertTrue(result.prices().containsKey(AAPL));
        assertTrue(result.prices().containsKey(GOOGL));
        assertEquals(3, result.prices().get(AAPL).size());
    }

    @Test
    void returns_partial_results_when_one_symbol_fails() {
        // Given one valid and one unknown symbol
        var symbols = List.of(AAPL, new StockSymbol("UNKNOWN"));

        // When fetching historical prices
        var result = service.fetchHistoricalPricesForSymbols(symbols, 3);

        // Then only the successful symbol is returned
        assertEquals(1, result.prices().size());
        assertTrue(result.prices().containsKey(AAPL));
    }

    @Test
    void returns_empty_map_when_all_symbols_fail() {
        // Given all unknown symbols
        var symbols = List.of(new StockSymbol("UNKNOWN1"), new StockSymbol("UNKNOWN2"));

        // When fetching historical prices
        var result = service.fetchHistoricalPricesForSymbols(symbols, 3);

        // Then empty map is returned
        assertTrue(result.prices().isEmpty());
    }

    @Test
    void parses_prices_correctly_for_symbol() {
        // Given a valid symbol
        var symbols = List.of(AAPL);

        // When fetching historical prices
        var result = service.fetchHistoricalPricesForSymbols(symbols, 3);

        // Then prices are correctly parsed
        StockPrice first = result.prices().get(AAPL).stream()
                .filter(p -> p.date().equals(LocalDate.of(2024, 1, 1)))
                .findFirst()
                .orElseThrow();
        assertEquals(new BigDecimal("180.00"), first.price());
        assertEquals(AAPL, first.symbol());
    }

    @Test
    void tracks_failed_symbols_when_one_symbol_fails() {
        // Given one valid and one unknown symbol
        var symbols = List.of(AAPL, new StockSymbol("UNKNOWN"));

        // When fetching historical prices
        var result = service.fetchHistoricalPricesForSymbols(symbols, 3);

        // Then failed symbol is tracked
        assertEquals(1, result.failedSymbols().size());
        assertTrue(result.failedSymbols().contains(new StockSymbol("UNKNOWN")));
    }

    private TimeSeriesResponse buildTimeSeriesResponse(String symbol, Map<String, String> datePrices) {
        TimeSeriesResponse.MetaData metaData = new TimeSeriesResponse.MetaData(
                "Daily Time Series",
                symbol,
                LocalDate.now().toString(),
                "compact",
                "US/Eastern"
        );

        Map<String, TimeSeriesResponse.DataPoint> timeSeries = new LinkedHashMap<>();
        datePrices.forEach((date, price) ->
                timeSeries.put(date, new TimeSeriesResponse.DataPoint(
                        price, price, price, price, "1000000"
                ))
        );

        return new TimeSeriesResponse(metaData, timeSeries, null, null, null);
    }
}
