package com.amraljundi.analyser.service;

import com.amraljundi.analyser.model.StockDataResult;
import com.amraljundi.analyser.model.StockSymbol;

import java.util.List;

public interface StockApiService {
    StockDataResult fetchHistoricalPricesForSymbols(List<StockSymbol> symbols, int days);
}
