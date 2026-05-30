package com.amraljundi.analyser.event;

import com.amraljundi.analyser.model.StockPrice;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public record SectorDataFetched(
        Map<String, List<StockPrice>> pricesBySymbol,
        LocalDate fetchedAt
) {
}
