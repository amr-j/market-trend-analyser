package com.amraljundi.analyser.client;

import com.amraljundi.analyser.exception.StockApiException;
import com.amraljundi.analyser.model.StockSymbol;
import com.amraljundi.analyser.model.TimeSeriesResponse;

import java.util.Map;

public class StockDataClientStub implements StockDataClient {

    private final Map<String, TimeSeriesResponse> responses;

    public StockDataClientStub(Map<String, TimeSeriesResponse> responses) {
        this.responses = responses;
    }

    @Override
    public TimeSeriesResponse getHistoricalPrices(StockSymbol symbol, int days) {
        if (!responses.containsKey(symbol.value())) {
            throw new StockApiException("No data for symbol: " + symbol.value());
        }
        return responses.get(symbol.value());
    }
}
