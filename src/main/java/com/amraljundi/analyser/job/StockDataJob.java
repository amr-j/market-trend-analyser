package com.amraljundi.analyser.job;

import com.amraljundi.analyser.config.JobConfig;
import com.amraljundi.analyser.entity.JobStatus;
import com.amraljundi.analyser.event.SectorDataFetched;
import com.amraljundi.analyser.exception.StockDataJobException;
import com.amraljundi.analyser.model.StockSymbol;
import com.amraljundi.analyser.repository.JobStatusRepository;
import com.amraljundi.analyser.service.StockApiService;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

import static java.time.LocalDate.now;

@Component
public class StockDataJob {
    private static final Logger log = LoggerFactory.getLogger(StockDataJob.class);

    private final JobConfig jobConfig;
    private final StockApiService stockApiService;
    private final JobStatusRepository jobStatusRepository;
    private final KafkaTemplate<String, SectorDataFetched> kafkaTemplate;

    public StockDataJob(
            JobConfig jobConfig,
            StockApiService stockApiService,
            JobStatusRepository jobStatusRepository,
            KafkaTemplate<String, SectorDataFetched> kafkaTemplate
    ) {
        this.jobConfig = jobConfig;
        this.stockApiService = stockApiService;
        this.jobStatusRepository = jobStatusRepository;
        this.kafkaTemplate = kafkaTemplate;
    }

    @Scheduled(cron = "0 0 * * * *")
    @SchedulerLock(name = "stockDataJob", lockAtMostFor = "PT1H", lockAtLeastFor = "PT30M")
    public void run() {
        log.info("Stock data job started");

        final var today = now();
        final var symbolsToFetch = jobConfig.symbols().stream()
                .map(StockSymbol::new)
                .filter(symbol -> needsFetching(symbol, today))
                .toList();

        if (symbolsToFetch.isEmpty()) {
            log.info("All symbols already fetched for today, skipping");
            return;
        }

        log.info("Fetching data for {} symbols", symbolsToFetch.size());

        final var result = stockApiService.fetchHistoricalPricesForSymbols(symbolsToFetch, jobConfig.lookbackDays());

        if (!result.failedSymbols().isEmpty()) {
            log.error("Failed to fetch data for symbols: {} - aborting job", result.failedSymbols());
            throw new StockDataJobException("Job aborted due to failed symbols: " + result.failedSymbols());
        }

        kafkaTemplate.send("sector-data-fetched", new SectorDataFetched(result.prices(), today));
        log.info("Published SectorDataFetched event for {} symbols", result.prices().size());

        result.prices().keySet().forEach(symbol -> {
            final var status = jobStatusRepository.findById(symbol.value())
                    .orElse(new JobStatus(symbol.value(), today));
            status.updateLastFetched(today);
            jobStatusRepository.save(status);
        });

        log.info("Stock data job completed successfully");
    }

    private boolean needsFetching(StockSymbol symbol, LocalDate today) {
        return jobStatusRepository.findById(symbol.value())
                .map(status -> status.lastFetched().isBefore(today))
                .orElse(true);
    }
}
