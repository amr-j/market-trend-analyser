package com.amraljundi.analyser.controller;

import com.amraljundi.analyser.model.MarketTrendReport;
import com.amraljundi.analyser.model.StockSymbol;
import com.amraljundi.analyser.service.MarketTrendService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import static java.util.Objects.requireNonNull;

@RestController
@RequestMapping("/api/analyze")
public class MarketAnalysisController {
    private static final Logger log = LoggerFactory.getLogger(MarketAnalysisController.class);
    private static final int MAX_SYMBOLS = 10;
    private static final int MIN_MOMENTUM_PERIOD = 1;
    private static final int MAX_MOMENTUM_PERIOD = 100;

    private final MarketTrendService marketTrendService;

    public MarketAnalysisController(MarketTrendService marketTrendService) {
        requireNonNull(marketTrendService, "MarketTrendService cannot be null");
        this.marketTrendService = marketTrendService;
    }

    @GetMapping("/sector")
    public MarketTrendReport analyzeSector(
            @RequestParam List<StockSymbol> symbols,
            @RequestParam(defaultValue = "14") int momentumPeriod) {

        validateSymbols(symbols);
        validateMomentumPeriod(momentumPeriod);

        log.info("Analyzing sector for symbols: {}", symbols);
        return marketTrendService.analyze(symbols, momentumPeriod);
    }

    private void validateSymbols(List<StockSymbol> symbols) {
        if (symbols == null || symbols.isEmpty()) {
            throw new IllegalArgumentException("Symbols list cannot be empty");
        }
        if (symbols.size() > MAX_SYMBOLS) {
            throw new IllegalArgumentException("Cannot analyze more than " + MAX_SYMBOLS + " symbols at once");
        }
    }

    private void validateMomentumPeriod(int momentumPeriod) {
        if (momentumPeriod < MIN_MOMENTUM_PERIOD || momentumPeriod > MAX_MOMENTUM_PERIOD) {
            throw new IllegalArgumentException("Momentum period must be between " + MIN_MOMENTUM_PERIOD + " and " + MAX_MOMENTUM_PERIOD);
        }
    }
}
