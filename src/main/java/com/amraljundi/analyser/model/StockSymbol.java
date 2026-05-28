package com.amraljundi.analyser.model;

import com.fasterxml.jackson.annotation.JsonValue;

public record StockSymbol(String value) {
    public StockSymbol {
        if (value == null || value.isBlank()) throw new IllegalArgumentException("Symbol cannot be blank");
        value = value.toUpperCase();
    }

    @JsonValue
    public String value() {
        return value;
    }
}
