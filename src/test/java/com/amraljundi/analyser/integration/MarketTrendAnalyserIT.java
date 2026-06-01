package com.amraljundi.analyser.integration;

import com.amraljundi.analyser.job.StockDataJob;
import com.amraljundi.analyser.repository.JobStatusRepository;
import com.amraljundi.analyser.repository.MarketAnalysisRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import static java.time.LocalDate.now;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = {
        "stock.api.mode=mock",
        "job.symbols=AAPL,GOOGL,MSFT",
        "job.lookback-days=14"
})
class MarketTrendAnalyserIT {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private StockDataJob job;

    @Autowired
    private MarketAnalysisRepository marketAnalysisRepository;

    @Autowired
    private JobStatusRepository jobStatusRepository;

    @BeforeEach
    void setUp() {
        marketAnalysisRepository.deleteAll();
        jobStatusRepository.deleteAll();
    }

    @Test
    void full_flow_job_publishes_consumer_saves_endpoint_returns() throws Exception {
        // Given job runs and publishes to Kafka
        job.run();

        // When consumer processes and saves to DB
        await().atMost(10, SECONDS).until(() ->
                marketAnalysisRepository.count() > 0
        );

        // Then endpoint returns momentum for saved symbols
        mockMvc.perform(get("/api/analysis/momentum")
                        .param("symbols", "AAPL", "GOOGL", "MSFT")
                        .param("from", now().minusDays(14).toString())
                        .param("to", now().toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.from").value(now().minusDays(14).toString()))
                .andExpect(jsonPath("$.to").value(now().toString()))
                .andExpect(jsonPath("$.stockMomentums.AAPL").isArray())
                .andExpect(jsonPath("$.stockMomentums.AAPL[0].direction").exists())
                .andExpect(jsonPath("$.stockMomentums.AAPL[0].percentChange").exists())
                .andExpect(jsonPath("$.stockMomentums.AAPL[0].analyzedAt").exists())
                .andExpect(jsonPath("$.stockMomentums.GOOGL").isArray())
                .andExpect(jsonPath("$.stockMomentums.MSFT").isArray());
    }

    @Test
    void job_does_not_reprocess_already_fetched_symbols() throws Exception {
        // Given job already ran today
        job.run();
        await().atMost(10, SECONDS).until(() ->
                marketAnalysisRepository.count() > 0
        );
        var countAfterFirstRun = marketAnalysisRepository.count();

        // When job runs again
        job.run();
        Thread.sleep(3000);

        // Then no new records are saved
        assertEquals(countAfterFirstRun, marketAnalysisRepository.count());
    }
}
