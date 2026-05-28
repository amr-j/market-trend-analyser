package com.amraljundi.analyser.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Map;

public record TimeSeriesResponse(
        MetaData metaData,
        Map<String, DataPoint> timeSeries,
        String information,
        String note,
        String errorMessage
) {
    public record MetaData(
            @JsonProperty("1. Information") String information,
            @JsonProperty("2. Symbol") String symbol,
            @JsonProperty("3. Last Refreshed") String lastRefreshed,
            @JsonProperty("5. Output Size") String outputSize,
            @JsonProperty("6. Time Zone") String timeZone
    ) {}

    public record DataPoint(
            @JsonProperty("1. open") String open,
            @JsonProperty("2. high") String high,
            @JsonProperty("3. low") String low,
            @JsonProperty("4. close") String close,
            @JsonProperty("5. volume") String volume
    ) {}
}
