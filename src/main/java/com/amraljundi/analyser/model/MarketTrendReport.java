package com.amraljundi.analyser.model;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public record MarketTrendReport(
        TrendDirection sectorTrend,
        double confidence,
        LocalDateTime analyzedAt,
        Map<StockSymbol, Momentum> stockMomentums,
        List<StockSymbol> failedSymbols,
        String recommendation
) {}
