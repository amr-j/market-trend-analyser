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
    void returns_momentum_report_for_valid_symbols() throws Exception {
        // Given valid symbols and date range
        // When getting momentum
        mockMvc.perform(get("/api/analysis/momentum")
                        .param("symbols", "AAPL", "GOOGL", "MSFT")
                        .param("from", "2024-01-01")
                        .param("to", "2024-01-14"))
                // Then a valid report is returned
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.from").value("2024-01-01"))
                .andExpect(jsonPath("$.to").value("2024-01-14"))
                .andExpect(jsonPath("$.stockMomentums").exists())
                .andExpect(jsonPath("$.stockMomentums.AAPL").exists())
                .andExpect(jsonPath("$.stockMomentums.GOOGL").exists())
                .andExpect(jsonPath("$.stockMomentums.MSFT").exists());
    }

    @Test
    void returns_400_when_symbols_list_is_empty() throws Exception {
        // Given empty symbols list
        // When getting momentum
        mockMvc.perform(get("/api/analysis/momentum")
                        .param("symbols", "")
                        .param("from", "2024-01-01")
                        .param("to", "2024-01-14"))
                // Then bad request is returned
                .andExpect(status().isBadRequest());
    }

    @Test
    void returns_400_when_too_many_symbols() throws Exception {
        // Given 11 symbols exceeding max limit
        // When getting momentum
        mockMvc.perform(get("/api/analysis/momentum")
                        .param("symbols", "AAPL", "GOOGL", "MSFT", "AMZN", "META",
                                "NFLX", "TSLA", "NVDA", "AMD", "INTC", "ORCL")
                        .param("from", "2024-01-01")
                        .param("to", "2024-01-14"))
                // Then bad request is returned
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Cannot analyze more than 10 symbols at once"));
    }

    @Test
    void returns_400_when_from_date_is_after_to_date() throws Exception {
        // Given invalid date range
        // When getting momentum
        mockMvc.perform(get("/api/analysis/momentum")
                        .param("symbols", "AAPL")
                        .param("from", "2024-01-14")
                        .param("to", "2024-01-01"))
                // Then bad request is returned
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("From date cannot be after to date"));
    }

    @Test
    void returns_empty_momentum_for_symbol_with_no_data() throws Exception {
        // Given a symbol with no data in DB
        // When getting momentum
        mockMvc.perform(get("/api/analysis/momentum")
                        .param("symbols", "AAPL")
                        .param("from", "2024-01-01")
                        .param("to", "2024-01-14"))
                // Then empty list is returned for that symbol
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.stockMomentums.AAPL").isArray())
                .andExpect(jsonPath("$.stockMomentums.AAPL").isEmpty());
    }
}
