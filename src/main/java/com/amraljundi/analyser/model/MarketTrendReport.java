package com.amraljundi.analyser.model;

import com.amraljundi.analyser.entity.MarketAnalysis;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public record MarketTrendReport(
        LocalDate from,
        LocalDate to,
        Map<StockSymbol, List<MarketAnalysis>> stockMomentums
) {}
