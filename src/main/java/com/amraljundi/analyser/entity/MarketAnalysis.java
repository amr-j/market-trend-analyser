package com.amraljundi.analyser.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "market_analysis")
public class MarketAnalysis {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "symbol", nullable = false)
    @JsonProperty
    private String symbol;

    @Column(name = "direction", nullable = false)
    @JsonProperty
    private String direction;

    @Column(name = "percent_change", nullable = false)
    @JsonProperty
    private BigDecimal percentChange;

    @Column(name = "analyzed_at", nullable = false)
    @JsonProperty
    private LocalDate analyzedAt;

    protected MarketAnalysis() {
    }

    public MarketAnalysis(String symbol, String direction, BigDecimal percentChange, LocalDate analyzedAt) {
        this.symbol = symbol;
        this.direction = direction;
        this.percentChange = percentChange;
        this.analyzedAt = analyzedAt;
    }

    public String symbol() {
        return symbol;
    }

    public String direction() {
        return direction;
    }

    public BigDecimal percentChange() {
        return percentChange;
    }

    public LocalDate analyzedAt() {
        return analyzedAt;
    }
}
