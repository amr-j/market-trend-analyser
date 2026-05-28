package com.amraljundi.analyser.util;

import com.amraljundi.analyser.model.MarketTrendReport;
import com.amraljundi.analyser.model.Momentum;
import com.amraljundi.analyser.model.StockSymbol;
import com.amraljundi.analyser.model.TrendDirection;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static com.amraljundi.analyser.model.TrendDirection.BEARISH;
import static com.amraljundi.analyser.model.TrendDirection.BULLISH;
import static com.amraljundi.analyser.model.TrendDirection.NEUTRAL;
import static java.lang.String.format;
import static java.time.temporal.ChronoUnit.SECONDS;

public final class TrendAnalyzer {

    public static MarketTrendReport analyze(Map<StockSymbol, Momentum> momentums, List<StockSymbol> failedSymbols) {
        final var bullishCount = momentums.values().stream()
                .filter(m -> m.direction() == BULLISH)
                .count();

        final var total = momentums.size();
        final var bullishRatio = (double) bullishCount / total;

        final var sectorTrend = determineSectorTrend(bullishRatio);
        final var confidence = calculateConfidence(bullishCount, total);
        final var recommendation = generateRecommendation(sectorTrend, bullishCount, total, confidence);

        return new MarketTrendReport(sectorTrend, confidence, LocalDateTime.now().truncatedTo(SECONDS), momentums, failedSymbols, recommendation);
    }

    private static TrendDirection determineSectorTrend(double bullishRatio) {
        if (bullishRatio >= 0.7) return BULLISH;
        if (bullishRatio <= 0.3) return BEARISH;
        return NEUTRAL;
    }

    private static double calculateConfidence(long bullishCount, long total) {
        final var majorityCount = Math.max(bullishCount, total - bullishCount);
        return Math.round((double) majorityCount / total * 100.0) / 100.0;
    }

    private static String generateRecommendation(TrendDirection trend, long bullishCount, long total, double confidence) {
        return switch (trend) {
            case BULLISH -> format("Strong upward momentum. %d/%d stocks bullish. Confidence: %.0f%%", bullishCount, total, confidence * 100);
            case BEARISH -> format("Downward trend identified. %d/%d stocks bearish. Confidence: %.0f%%", total - bullishCount, total, confidence * 100);
            case NEUTRAL -> format("Mixed signals. No clear direction. Confidence: %.0f%%", confidence * 100);
        };
    }
}
