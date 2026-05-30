package com.amraljundi.analyser.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.LocalDate;

@Entity
@Table(name = "job_status")
public class JobStatus {

    @Id
    @Column(name = "symbol")
    private String symbol;

    @Column(name = "last_fetched", nullable = false)
    private LocalDate lastFetched;

    protected JobStatus() {
    }

    public JobStatus(String symbol, LocalDate lastFetched) {
        this.symbol = symbol;
        this.lastFetched = lastFetched;
    }

    public String symbol() {
        return symbol;
    }

    public LocalDate lastFetched() {
        return lastFetched;
    }

    public void updateLastFetched(LocalDate date) {
        this.lastFetched = date;
    }
}
