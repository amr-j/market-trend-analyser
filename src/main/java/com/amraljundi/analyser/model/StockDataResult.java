package com.amraljundi.analyser.model;

import java.util.List;
import java.util.Map;

public record StockDataResult(
        Map<StockSymbol, List<StockPrice>> prices,
        List<StockSymbol> failedSymbols
) {}
