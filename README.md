# Market Trend Analyser

A Spring Boot service that analyses stock sector trends using concurrent data fetching and momentum-based calculations.

## Prerequisites

- Java 21
- Maven

## Running

Build the project:
```bash
make build
```

Run in mock mode (default, no API key required):
```bash
make run
```

Run with real Alpha Vantage data:
```bash
make run-real API_KEY=your-key-here
```

Run the tests:
```bash
make test
```

## Usage

See `api-docs.yaml` for the full OpenAPI spec. Open in [Swagger Editor](https://editor.swagger.io) to explore and test the API interactively.

## Design

The analyser is composed of the following components:

- **`StockDataClient`**: interface with two implementations — `MockStockDataClient` for local/testing use and `AlphaVantageClient` for real market data. Switched via `stock.api.mode` property.
- **`StockApiService`**: fetches historical prices for multiple symbols concurrently using Java 21 virtual threads. Partial failures are handled gracefully: if one symbol fails, the rest are still returned.
- **`MomentumCalculator`**: calculates price momentum for a symbol by comparing the most recent price to the price N days ago, returning a direction (BULLISH/BEARISH/NEUTRAL) and percent change.
- **`TrendAnalyzer`**: aggregates per-symbol momentum into a sector-wide trend, confidence score, and recommendation.
- **`MarketTrendService`**: orchestrates the full analysis pipeline.

## Key Technical Decisions

- **Virtual threads** for concurrent stock data fetching
- **Conditional beans** (`@ConditionalOnProperty`) to switch between mock and real data clients cleanly at the Spring context level.
- **Rate limiter** `AlphaVantageClient` configured for the free tier (5 requests/minute) to prevent API abuse.

## Constraints

- **Alpha Vantage free tier**: limited to 25 requests/day and 1 request/second. Concurrent requests for multiple symbols may trigger rate limiting. Mock mode is recommended for testing and demonstration.
- **Momentum only**: the analysis uses price momentum over a configurable period. More sophisticated indicators (RSI, MACD, moving average crossovers) could be added as additional calculators.
