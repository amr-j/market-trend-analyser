package com.amraljundi.analyser.client;

import com.amraljundi.analyser.model.StockSymbol;
import com.amraljundi.analyser.model.TimeSeriesResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Random;

import static java.lang.String.valueOf;
import static java.math.RoundingMode.HALF_UP;
import static java.time.LocalDate.now;

@Component
@ConditionalOnProperty(name = "stock.api.mode", havingValue = "mock", matchIfMissing = true)
public class MockStockDataClient implements StockDataClient {
    private static final Logger log = LoggerFactory.getLogger(MockStockDataClient.class);

    private static final BigDecimal DEFAULT_PRICE = new BigDecimal("100.00");
    private static final Map<String, BigDecimal> BASE_PRICES = Map.of(
            "AAPL", new BigDecimal("180.00"),
            "GOOGL", new BigDecimal("140.00"),
            "MSFT", new BigDecimal("370.00"),
            "AMZN", new BigDecimal("170.00"),
            "META", new BigDecimal("450.00"),
            "NFLX", new BigDecimal("480.00"),
            "TSLA", new BigDecimal("240.00")
    );

    @Override
    public TimeSeriesResponse getHistoricalPrices(StockSymbol symbol, int days) {
        log.info("Generating mock data for symbol: {}", symbol.value());

        final var timeSeries = new LinkedHashMap<String, TimeSeriesResponse.DataPoint>();
        var price = BASE_PRICES.getOrDefault(symbol.value(), DEFAULT_PRICE);
        final var random = new Random();

        for (int i = 0; i < days; i++) {
            final var date = now().minusDays(i);
            double changePercent = (random.nextDouble() - 0.5) * 0.1;
            price = price.multiply(BigDecimal.valueOf(1 + changePercent))
                    .setScale(2, HALF_UP);

            timeSeries.put(date.toString(), new TimeSeriesResponse.DataPoint(
                    price.toString(),
                    price.toString(),
                    price.toString(),
                    price.toString(),
                    valueOf(1000000 + random.nextInt(500000))
            ));
        }

        final var metaData = new TimeSeriesResponse.MetaData(
                "Mock Daily Time Series",
                symbol.value(),
                now().toString(),
                "compact",
                "US/Eastern"
        );

        log.info("Generated {} mock data points for symbol: {}", days, symbol.value());
        return new TimeSeriesResponse(metaData, timeSeries, null, null, null);
    }
}
