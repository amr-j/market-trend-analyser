package com.amraljundi.analyser.service;

import com.amraljundi.analyser.entity.MarketAnalysis;
import com.amraljundi.analyser.model.StockSymbol;
import com.amraljundi.analyser.repository.MarketAnalysisRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class MarketTrendServiceTest {

    private static final StockSymbol AAPL = new StockSymbol("AAPL");
    private static final StockSymbol GOOGL = new StockSymbol("GOOGL");

    private MarketTrendService service;
    private MarketAnalysisRepository repository;

    @BeforeEach
    void setUp() {
        repository = mock(MarketAnalysisRepository.class);
        service = new MarketTrendService(repository);
    }

    @Test
    void returns_momentum_report_for_all_symbols() {
        // Given analyses exist for both symbols
        var from = LocalDate.of(2024, 1, 1);
        var to = LocalDate.of(2024, 1, 14);

        when(repository.findBySymbolAndAnalyzedAtBetween("AAPL", from, to))
                .thenReturn(List.of(new MarketAnalysis("AAPL", "BULLISH", new BigDecimal("5.00"), from)));
        when(repository.findBySymbolAndAnalyzedAtBetween("GOOGL", from, to))
                .thenReturn(List.of(new MarketAnalysis("GOOGL", "BEARISH", new BigDecimal("-3.00"), from)));

        // When getting momentum
        var result = service.getMomentum(List.of(AAPL, GOOGL), from, to);

        // Then report contains both symbols
        assertEquals(from, result.from());
        assertEquals(to, result.to());
        assertEquals(2, result.stockMomentums().size());
        assertEquals("BULLISH", result.stockMomentums().get(AAPL).getFirst().direction());
    }

    @Test
    void returns_empty_list_for_symbol_with_no_data() {
        // Given no analyses for AAPL
        var from = LocalDate.of(2024, 1, 1);
        var to = LocalDate.of(2024, 1, 14);

        when(repository.findBySymbolAndAnalyzedAtBetween("AAPL", from, to))
                .thenReturn(List.of());

        // When getting momentum
        var result = service.getMomentum(List.of(AAPL), from, to);

        // Then empty list is returned for that symbol
        assertTrue(result.stockMomentums().get(AAPL).isEmpty());
    }
}
