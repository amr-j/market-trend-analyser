package com.amraljundi.analyser.client;

import com.amraljundi.analyser.exception.StockApiException;
import com.amraljundi.analyser.model.StockSymbol;
import com.amraljundi.analyser.model.TimeSeriesResponse;
import com.amraljundi.analyser.model.TimeSeriesResponseBuilder;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

@Component
@ConditionalOnProperty(name = "stock.api.mode", havingValue = "real")
public class AlphaVantageClient implements StockDataClient {
    private static final Logger log = LoggerFactory.getLogger(AlphaVantageClient.class);

    private final String apiKey;
    private final RestClient restClient;
    private static final String TIME_SERIES_DAILY = "TIME_SERIES_DAILY";

    public AlphaVantageClient(
            @Value("${alphavantage.api.key}") String apiKey,
            @Value("${alphavantage.api.base-url}") String baseUrl
    ) {
        if (apiKey == null || apiKey.isBlank()) throw new IllegalArgumentException("Alpha Vantage API key must be configured");
        if (baseUrl == null || baseUrl.isBlank()) throw new IllegalArgumentException("Alpha Vantage base URL must be configured");
        this.apiKey = apiKey;
        this.restClient = RestClient.builder()
                .baseUrl(baseUrl)
                .build();
    }

    @Override
    @RateLimiter(name = "alphavantage")
    public TimeSeriesResponse getHistoricalPrices(StockSymbol symbol, int days) {
        log.debug("Fetching time series for symbol: {} with interval: {}", symbol.value(), days);

        try {
            final var builder = restClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .queryParam("function", TIME_SERIES_DAILY)
                            .queryParam("symbol", symbol.value())
                            .queryParam("outputsize", days > 100 ? "full" : "compact")
                            .queryParam("apikey", apiKey)
                            .build())
                    .retrieve()
                    .body(TimeSeriesResponseBuilder.class);

            if (builder == null) {
                throw new StockApiException("Null response from API for symbol: " + symbol.value());
            }

            final var response = builder.build();

            validate(response, symbol);

            log.info("Successfully fetched {} data points for symbol: {}", response.timeSeries().size(), symbol.value());
            return response;

        } catch (RestClientException e) {
            log.error("REST client error for symbol {}: {}", symbol.value(), e.getMessage(), e);
            throw new StockApiException("Failed to fetch data for symbol: " + symbol.value(), e);
        }
    }

    private void validate(TimeSeriesResponse response, StockSymbol symbol) {
        if (response.errorMessage() != null) {
            throw new StockApiException("Invalid API call for symbol: " + symbol.value());
        }
        if (response.note() != null || response.information() != null) {
            throw new StockApiException("Alpha Vantage rate limit reached for symbol: " + symbol.value());
        }
        if (response.timeSeries().isEmpty()) {
            throw new StockApiException("Empty time series for symbol: " + symbol.value());
        }
        if (response.metaData() == null) {
            throw new StockApiException("Missing metadata for symbol: " + symbol.value());
        }
    }
}
