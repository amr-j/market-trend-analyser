package com.amraljundi.analyser.controller;

import com.amraljundi.analyser.model.MarketTrendReport;
import com.amraljundi.analyser.model.StockSymbol;
import com.amraljundi.analyser.service.MarketTrendService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

import static java.util.Objects.requireNonNull;

@RestController
@RequestMapping("/api/analysis")
public class MarketAnalysisController {
    private static final Logger log = LoggerFactory.getLogger(MarketAnalysisController.class);
    private static final int MAX_SYMBOLS = 10;

    private final MarketTrendService marketTrendService;

    public MarketAnalysisController(MarketTrendService marketTrendService) {
        requireNonNull(marketTrendService, "MarketTrendService cannot be null");
        this.marketTrendService = marketTrendService;
    }

    @GetMapping("/momentum")
    public MarketTrendReport getMomentum(
            @RequestParam List<StockSymbol> symbols,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {

        validateSymbols(symbols);
        validateDateRange(from, to);

        log.info("Getting momentum for symbols: {} from {} to {}", symbols, from, to);
        return marketTrendService.getMomentum(symbols, from, to);
    }

    private void validateSymbols(List<StockSymbol> symbols) {
        if (symbols == null || symbols.isEmpty()) {
            throw new IllegalArgumentException("Symbols list cannot be empty");
        }
        if (symbols.size() > MAX_SYMBOLS) {
            throw new IllegalArgumentException("Cannot analyze more than " + MAX_SYMBOLS + " symbols at once");
        }
    }

    private void validateDateRange(LocalDate from, LocalDate to) {
        if (from == null || to == null) {
            throw new IllegalArgumentException("Date range cannot be null");
        }
        if (from.isAfter(to)) {
            throw new IllegalArgumentException("From date cannot be after to date");
        }
    }
}
