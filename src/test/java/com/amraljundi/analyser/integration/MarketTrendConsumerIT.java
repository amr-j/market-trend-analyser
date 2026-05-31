package com.amraljundi.analyser.integration;

import com.amraljundi.analyser.event.SectorDataFetched;
import com.amraljundi.analyser.model.StockPrice;
import com.amraljundi.analyser.model.StockSymbol;
import com.amraljundi.analyser.repository.MarketAnalysisRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.test.context.TestPropertySource;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static java.time.LocalDate.now;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@TestPropertySource(properties = {
        "stock.api.mode=mock",
        "job.symbols=AAPL,GOOGL,MSFT",
        "job.lookback-days=14"
})
class MarketTrendConsumerIT {

    private static final StockSymbol AAPL = new StockSymbol("AAPL");
    private static final StockSymbol GOOGL = new StockSymbol("GOOGL");

    @Autowired
    private KafkaTemplate<String, SectorDataFetched> kafkaTemplate;

    @Autowired
    private MarketAnalysisRepository marketAnalysisRepository;

    @BeforeEach
    void setUp() {
        marketAnalysisRepository.deleteAll();
    }

    @Test
    void saves_analysis_for_all_symbols_when_event_received() throws InterruptedException {
        // Given a sector data event with two symbols
        var prices = Map.of(
                AAPL, buildPrices(AAPL, "100.00", "110.00"),
                GOOGL, buildPrices(GOOGL, "140.00", "130.00")
        );
        var event = new SectorDataFetched(prices, now());

        // When event is published
        kafkaTemplate.send("sector-data-fetched", event);

        // Then analyses are saved for all symbols
        await().atMost(10, SECONDS).untilAsserted(() -> {
            var analyses = marketAnalysisRepository.findAll();
            assertEquals(2, analyses.size());
            assertTrue(analyses.stream().anyMatch(a -> a.symbol().equals("AAPL")));
            assertTrue(analyses.stream().anyMatch(a -> a.symbol().equals("GOOGL")));
        });
    }

    @Test
    void saves_correct_direction_for_bullish_symbol() throws InterruptedException {
        // Given AAPL with increasing price
        var prices = Map.of(
                AAPL, buildPrices(AAPL, "100.00", "110.00")
        );
        var event = new SectorDataFetched(prices, now());

        // When event is published
        kafkaTemplate.send("sector-data-fetched", event);

        // Then AAPL is saved as bullish
        await().atMost(10, SECONDS).untilAsserted(() -> {
            var analyses = marketAnalysisRepository.findAll();
            assertEquals(1, analyses.size());
            assertEquals("BULLISH", analyses.getFirst().direction());
        });
    }

    @Test
    void saves_correct_direction_for_bearish_symbol() throws InterruptedException {
        // Given GOOGL with decreasing price
        var prices = Map.of(
                GOOGL, buildPrices(GOOGL, "140.00", "130.00")
        );
        var event = new SectorDataFetched(prices, now());

        // When event is published
        kafkaTemplate.send("sector-data-fetched", event);

        // Then GOOGL is saved as bearish
        await().atMost(10, SECONDS).untilAsserted(() -> {
            var analyses = marketAnalysisRepository.findAll();
            assertEquals(1, analyses.size());
            assertEquals("BEARISH", analyses.getFirst().direction());
        });
    }

    private List<StockPrice> buildPrices(StockSymbol symbol, String startPrice, String endPrice) {
        return List.of(
                new StockPrice(symbol, new BigDecimal(startPrice), LocalDate.of(2024, 1, 1), 1000000),
                new StockPrice(symbol, new BigDecimal(endPrice), LocalDate.of(2024, 1, 2), 1000000)
        );
    }
}
