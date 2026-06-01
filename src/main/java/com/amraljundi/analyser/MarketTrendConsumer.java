package com.amraljundi.analyser;

import com.amraljundi.analyser.entity.MarketAnalysis;
import com.amraljundi.analyser.event.SectorDataFetched;
import com.amraljundi.analyser.repository.MarketAnalysisRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

import static com.amraljundi.analyser.util.MomentumCalculator.calculateMomentum;

@Component
public class MarketTrendConsumer {
    private static final Logger log = LoggerFactory.getLogger(MarketTrendConsumer.class);

    private final MarketAnalysisRepository marketAnalysisRepository;

    public MarketTrendConsumer(MarketAnalysisRepository marketAnalysisRepository) {
        this.marketAnalysisRepository = marketAnalysisRepository;
    }

    @KafkaListener(topics = "sector-data-fetched", groupId = "market-analyser")
    public void consume(SectorDataFetched event, Acknowledgment acknowledgment) {
        log.info("Received SectorDataFetched event for {} symbols on {}",
                event.pricesBySymbol().size(), event.fetchedAt());

        try {
            final var analyses = event.pricesBySymbol().entrySet().stream()
                    .map(entry -> {
                        final var momentum = calculateMomentum(
                                entry.getKey(),
                                entry.getValue(),
                                entry.getValue().size() - 1
                        );
                        return new MarketAnalysis(
                                entry.getKey().value(),
                                momentum.direction().name(),
                                momentum.percentChange(),
                                event.fetchedAt()
                        );
                    })
                    .toList();

            marketAnalysisRepository.saveAll(analyses);
            log.info("Saved {} analyses for date {}", analyses.size(), event.fetchedAt());

            acknowledgment.acknowledge();
            log.info("Offset committed for {} symbols", event.pricesBySymbol().size());

        } catch (DataIntegrityViolationException e) {
            log.warn("Duplicate data detected, skipping: {}", e.getMessage());
            acknowledgment.acknowledge();
        } catch (Exception e) {
            log.error("Failed to process SectorDataFetched event: {}", e.getMessage(), e);
            throw e;
        }
    }
}
