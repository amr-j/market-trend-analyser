package com.amraljundi.analyser.model;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.math.BigDecimal;

public record Momentum(
        @JsonIgnore StockSymbol symbol,
        TrendDirection direction,
        BigDecimal percentChange
) {}
