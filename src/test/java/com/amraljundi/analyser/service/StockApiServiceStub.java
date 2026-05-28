package com.amraljundi.analyser.service;

import com.amraljundi.analyser.model.StockDataResult;
import com.amraljundi.analyser.model.StockPrice;
import com.amraljundi.analyser.model.StockSymbol;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class StockApiServiceStub implements StockApiService {

    private final Map<StockSymbol, List<StockPrice>> responses;

    public StockApiServiceStub(Map<StockSymbol, List<StockPrice>> responses) {
        this.responses = responses;
    }

    @Override
    public StockDataResult fetchHistoricalPricesForSymbols(List<StockSymbol> symbols, int days) {
        var prices = symbols.stream()
                .filter(responses::containsKey)
                .collect(Collectors.toMap(s -> s, responses::get));

        var failedSymbols = symbols.stream()
                .filter(s -> !responses.containsKey(s))
                .toList();

        return new StockDataResult(prices, failedSymbols);
    }
}
