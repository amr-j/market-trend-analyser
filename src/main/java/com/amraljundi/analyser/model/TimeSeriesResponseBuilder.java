package com.amraljundi.analyser.model;

import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.LinkedHashMap;
import java.util.Map;

public class TimeSeriesResponseBuilder {

    @JsonProperty("Meta Data")
    private TimeSeriesResponse.MetaData metaData;

    @JsonProperty("Information")
    private String information;

    @JsonProperty("Note")
    private String note;

    @JsonProperty("Error Message")
    private String errorMessage;

    private final Map<String, TimeSeriesResponse.DataPoint> timeSeries = new LinkedHashMap<>();

    @JsonAnySetter
    public void setDynamicProperty(String key, Map<String, Map<String, String>> value) {
        if (key.startsWith("Time Series")) {
            value.forEach((timestamp, data) -> timeSeries.put(timestamp,
                    new TimeSeriesResponse.DataPoint(
                            data.get("1. open"),
                            data.get("2. high"),
                            data.get("3. low"),
                            data.get("4. close"),
                            data.get("5. volume")
                    )
            ));
        }
    }

    public TimeSeriesResponse build() {
        return new TimeSeriesResponse(metaData, timeSeries, information, note, errorMessage);
    }
}
