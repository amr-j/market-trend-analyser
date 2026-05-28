package com.amraljundi.analyser.service;

import com.amraljundi.analyser.client.StockDataClient;
import com.amraljundi.analyser.model.StockDataResult;
import com.amraljundi.analyser.model.StockPrice;
import com.amraljundi.analyser.model.StockSymbol;
import com.amraljundi.analyser.model.TimeSeriesResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.stream.Collectors;

import static java.util.Objects.requireNonNull;

@Service
public class StockApiServiceImpl implements StockApiService {
    private static final Logger log = LoggerFactory.getLogger(StockApiServiceImpl.class);

    private final Executor virtualThreadsExecutor;
    private final StockDataClient stockDataClient;

    public StockApiServiceImpl(
            @Qualifier("virtualThreadExecutor") Executor virtualThreadExecutor,
            StockDataClient stockDataClient
    ) {
        requireNonNull(virtualThreadExecutor, "Virtual thread executor cannot be null");
        requireNonNull(stockDataClient, "Stock data client cannot be null");
        this.virtualThreadsExecutor = virtualThreadExecutor;
        this.stockDataClient = stockDataClient;
    }

    public StockDataResult fetchHistoricalPricesForSymbols(List<StockSymbol> symbols, int days) {
        log.info("Fetching historical prices for {} symbols", symbols.size());

        final var completed = symbols.stream()
                .map(symbol -> CompletableFuture
                        .supplyAsync(() -> Map.entry(symbol, fetchHistoricalPrices(symbol, days)), virtualThreadsExecutor)
                        .exceptionally(e -> {
                            log.error("Failed to fetch prices for {}: {}", symbol.value(), e.getMessage());
                            return Map.entry(symbol, List.<StockPrice>of());
                        }))
                .toList()
                .stream()
                .map(CompletableFuture::join)
                .toList();

        final var prices = completed.stream()
                .filter(entry -> !entry.getValue().isEmpty())
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

        final var failedSymbols = completed.stream()
                .filter(entry -> entry.getValue().isEmpty())
                .map(Map.Entry::getKey)
                .toList();

        return new StockDataResult(prices, failedSymbols);
    }

    private List<StockPrice> fetchHistoricalPrices(StockSymbol symbol, int days) {
        log.debug("Fetching historical prices for symbol: {}", symbol.value());
        final var response = stockDataClient.getHistoricalPrices(symbol, days);

        return response.timeSeries().entrySet().stream()
                .map(entry -> parseToStockPrice(symbol, entry))
                .toList();
    }

    private StockPrice parseToStockPrice(StockSymbol symbol, Map.Entry<String, TimeSeriesResponse.DataPoint> entry) {
        final var date = LocalDate.parse(entry.getKey());
        final var price = new BigDecimal(entry.getValue().close());
        final var volume = Long.parseLong(entry.getValue().volume());
        return new StockPrice(symbol, price, date, volume);
    }
}
