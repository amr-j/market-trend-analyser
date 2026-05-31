package com.amraljundi.analyser.event;

import com.amraljundi.analyser.model.StockPrice;
import com.amraljundi.analyser.model.StockSymbol;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public record SectorDataFetched(
        Map<StockSymbol, List<StockPrice>> pricesBySymbol,
        LocalDate fetchedAt
) {
}
