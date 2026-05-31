package com.amraljundi.analyser.service;

import com.amraljundi.analyser.model.MarketTrendReport;
import com.amraljundi.analyser.model.StockSymbol;
import com.amraljundi.analyser.repository.MarketAnalysisRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

import static java.util.Objects.requireNonNull;

@Service
public class MarketTrendService {
    private static final Logger log = LoggerFactory.getLogger(MarketTrendService.class);

    private final MarketAnalysisRepository marketAnalysisRepository;

    public MarketTrendService(MarketAnalysisRepository marketAnalysisRepository) {
        requireNonNull(marketAnalysisRepository, "MarketAnalysisRepository cannot be null");
        this.marketAnalysisRepository = marketAnalysisRepository;
    }

    public MarketTrendReport getMomentum(List<StockSymbol> symbols, LocalDate from, LocalDate to) {
        log.info("Getting momentum for {} symbols from {} to {}", symbols.size(), from, to);

        final var stockMomentums = symbols.stream()
                .collect(Collectors.toMap(
                        symbol -> symbol,
                        symbol -> marketAnalysisRepository.findBySymbolAndAnalyzedAtBetween(
                                symbol.value(), from, to)
                ));

        return new MarketTrendReport(from, to, stockMomentums);
    }
}