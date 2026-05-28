package com.amraljundi.analyser.client;

import com.amraljundi.analyser.model.StockSymbol;
import com.amraljundi.analyser.model.TimeSeriesResponse;

public interface StockDataClient {

    TimeSeriesResponse getHistoricalPrices(StockSymbol symbol, int days);
}
