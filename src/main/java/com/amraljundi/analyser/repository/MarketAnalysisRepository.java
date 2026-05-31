package com.amraljundi.analyser.repository;

import com.amraljundi.analyser.entity.MarketAnalysis;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface MarketAnalysisRepository extends JpaRepository<MarketAnalysis, Long> {

    List<MarketAnalysis> findBySymbolAndAnalyzedAtBetween(String symbol, LocalDate from, LocalDate to);
}
