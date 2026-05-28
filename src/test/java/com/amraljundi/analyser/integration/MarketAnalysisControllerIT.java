package com.amraljundi.analyser.integration;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = "stock.api.mode=mock")
class MarketAnalysisControllerIT {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void returns_sector_trend_report_for_valid_symbols() throws Exception {
        // Given valid symbols
        // When analyzing sector trend
        mockMvc.perform(get("/api/analyze/sector")
                        .param("symbols", "AAPL", "GOOGL", "MSFT"))
                // Then a valid report is returned
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sectorTrend").exists())
                .andExpect(jsonPath("$.confidence").exists())
                .andExpect(jsonPath("$.analyzedAt").exists())
                .andExpect(jsonPath("$.recommendation").exists())
                .andExpect(jsonPath("$.stockMomentums.AAPL").exists())
                .andExpect(jsonPath("$.stockMomentums.GOOGL").exists())
                .andExpect(jsonPath("$.stockMomentums.MSFT").exists());
    }

    @Test
    void returns_400_when_symbols_list_is_empty() throws Exception {
        // Given empty symbols list
        // When analyzing sector trend
        mockMvc.perform(get("/api/analyze/sector")
                        .param("symbols", ""))
                // Then bad request is returned
                .andExpect(status().isBadRequest());
    }

    @Test
    void returns_400_when_too_many_symbols() throws Exception {
        // Given 11 symbols exceeding max limit
        // When analyzing sector trend
        mockMvc.perform(get("/api/analyze/sector")
                        .param("symbols", "AAPL", "GOOGL", "MSFT", "AMZN", "META",
                                "NFLX", "TSLA", "NVDA", "AMD", "INTC", "ORCL"))
                // Then bad request is returned
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Cannot analyze more than 10 symbols at once"));
    }

    @Test
    void returns_400_when_momentum_period_out_of_range() throws Exception {
        // Given momentum period exceeding max
        // When analyzing sector trend
        mockMvc.perform(get("/api/analyze/sector")
                        .param("symbols", "AAPL")
                        .param("momentumPeriod", "101"))
                // Then bad request is returned
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Momentum period must be between 1 and 100"));
    }

    @Test
    void returns_partial_report_when_some_symbols_are_unknown() throws Exception {
        // Given one valid and one unknown symbol
        // When analyzing sector trend
        mockMvc.perform(get("/api/analyze/sector")
                        .param("symbols", "AAPL", "UNKNOWN"))
                // Then report contains only the known symbol
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.stockMomentums.AAPL").exists())
                .andExpect(jsonPath("$.stockMomentums.UNKNOWN").doesNotExist());
    }
}
