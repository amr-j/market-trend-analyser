package com.amraljundi.analyser.service;

import com.amraljundi.analyser.model.MarketTrendReport;
import com.amraljundi.analyser.model.StockSymbol;
import com.amraljundi.analyser.util.TrendAnalyzer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.amraljundi.analyser.util.MomentumCalculator.calculateMomentum;
import static java.util.Objects.requireNonNull;

@Service
public class MarketTrendService {
    private static final Logger log = LoggerFactory.getLogger(MarketTrendService.class);

    private final StockApiService stockApiServiceImpl;

    public MarketTrendService(StockApiService stockApiServiceImpl) {
        requireNonNull(stockApiServiceImpl, "StockApiService cannot be null");
        this.stockApiServiceImpl = stockApiServiceImpl;
    }

    public MarketTrendReport analyze(List<StockSymbol> symbols, int momentumPeriod) {
        log.info("Analyzing sector trend for {} symbols with momentum period {}", symbols.size(), momentumPeriod);

        final var result = stockApiServiceImpl.fetchHistoricalPricesForSymbols(symbols, momentumPeriod + 1);

        final var momentums = result.prices()
                .entrySet()
                .stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        entry -> calculateMomentum(entry.getKey(), entry.getValue(), momentumPeriod)
                ));

        return TrendAnalyzer.analyze(momentums, result.failedSymbols());
    }
}