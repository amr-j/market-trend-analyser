package com.amraljundi.analyser.integration;

import com.amraljundi.analyser.TestKafkaConsumer;
import com.amraljundi.analyser.entity.JobStatus;
import com.amraljundi.analyser.job.StockDataJob;
import com.amraljundi.analyser.model.StockSymbol;
import com.amraljundi.analyser.repository.JobStatusRepository;
import com.amraljundi.analyser.repository.MarketAnalysisRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.test.context.TestPropertySource;

import static java.time.LocalDate.now;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@EmbeddedKafka(partitions = 1, topics = "sector-data-fetched")
@TestPropertySource(properties = {
        "stock.api.mode=mock",
        "job.symbols=AAPL,GOOGL,MSFT",
        "job.lookback-days=14"
})
class StockDataJobIT {

    @Autowired
    private StockDataJob job;

    @Autowired
    private JobStatusRepository jobStatusRepository;

    @Autowired
    private TestKafkaConsumer testKafkaConsumer;

    @Autowired
    private MarketAnalysisRepository marketAnalysisRepository;

    @BeforeEach
    void setUp() {
        jobStatusRepository.deleteAll();
        marketAnalysisRepository.deleteAll();
        testKafkaConsumer.reset();
    }

    @Test
    void publishes_event_and_updates_job_status_for_all_symbols() throws InterruptedException {
        // Given no previous job runs
        jobStatusRepository.deleteAll();

        // When job runs
        job.run();

        // Then Kafka event is published
        var received = testKafkaConsumer.getLatch().await(10, SECONDS);
        assertTrue(received);
        assertEquals(1, testKafkaConsumer.getReceivedEvents().size());

        var event = testKafkaConsumer.getReceivedEvents().getFirst();
        assertEquals(3, event.pricesBySymbol().size());
        assertTrue(event.pricesBySymbol().containsKey(new StockSymbol("AAPL")));
        assertEquals(now(), event.fetchedAt());

        // Then job_status is updated for all symbols
        var aaplStatus = jobStatusRepository.findById("AAPL").orElseThrow();
        assertEquals(now(), aaplStatus.lastFetched());
    }

    @Test
    void skips_symbols_already_fetched_today() throws InterruptedException {
        // Given all symbols already fetched today
        jobStatusRepository.save(new JobStatus("AAPL", now()));
        jobStatusRepository.save(new JobStatus("GOOGL", now()));
        jobStatusRepository.save(new JobStatus("MSFT", now()));

        // When job runs
        job.run();

        // Then no Kafka event is published
        var received = testKafkaConsumer.getLatch().await(3, SECONDS);
        assertFalse(received);
        assertTrue(testKafkaConsumer.getReceivedEvents().isEmpty());
    }

    @Test
    void updates_job_status_only_for_fetched_symbols() throws InterruptedException {
        // Given one symbol already fetched today
        jobStatusRepository.deleteAll();
        jobStatusRepository.save(new JobStatus("AAPL", now()));

        // When job runs
        job.run();

        // Then job_status updated only for unfetched symbols
        var received = testKafkaConsumer.getLatch().await(10, SECONDS);
        assertTrue(received);

        var event = testKafkaConsumer.getReceivedEvents().getFirst();
        assertEquals(2, event.pricesBySymbol().size());
        assertFalse(event.pricesBySymbol().containsKey(new StockSymbol("AAPL")));
    }
}
