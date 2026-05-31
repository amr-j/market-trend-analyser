# Market Trend Analyser

A Spring Boot service that fetches stock price data, publishes it via Kafka, and stores momentum analysis in PostgreSQL for querying.

## Prerequisites

- Java 21
- Maven
- Docker

## Running

Start infrastructure (Kafka + PostgreSQL):
```bash
docker-compose up -d
```

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

Run integration tests (requires Docker):
```bash
make integration-test
```

## Usage

See `api-docs.yaml` for the full OpenAPI spec. Open in [Swagger Editor](https://editor.swagger.io) to explore and test the API interactively.

## Design

The analyser is composed of the following components:

- **`StockDataClient`**: interface with two implementations — `MockStockDataClient` for local/testing use and `AlphaVantageClient` for real market data. Switched via `stock.api.mode` property.
- **`StockDataJob`**: scheduled hourly job that fetched symbol data. Publishes a `SectorDataFetched` event to Kafka.
- **`MarketTrendConsumer`**: Kafka consumer that calculates momentum per symbol and saves results to PostgreSQL.
- **`StockApiService`**: fetches historical prices for multiple symbols concurrently using Java 21 virtual threads. Partial failures are handled gracefully.
- **`MomentumCalculator`**: calculates price momentum by comparing the most recent price to the price N days ago, returning a direction (BULLISH/BEARISH/NEUTRAL) and percent change.
- **`MarketTrendService`**: queries momentum analysis from DB by symbol and date range.

## Key Technical Decisions

- **Virtual threads** for concurrent stock data fetching. I/O bound operations benefit from lightweight concurrency without blocking platform threads.
- **Kafka** for async decoupling between data fetching and analysis — if the consumer fails, the message is redelivered and idempotency handles duplicates.
- **Conditional beans** (`@ConditionalOnProperty`) to switch between mock and real data clients cleanly at the Spring context level.
- **ShedLock** for distributed job coordination. This ensures exactly one node runs the job at a time.
- **Manual offset commit**:Kafka offset is committed only after successful DB write, guaranteeing at-least-once processing.
- **Idempotency** via unique constraint on `(symbol, analyzed_at)`.
- **Rate limiter** on `AlphaVantageClient` configured for the free tier limits.

## Constraints

- **Alpha Vantage free tier**: limited to 25 requests/day and 1 request/second. Concurrent requests for multiple symbols may trigger rate limiting. Mock mode is recommended for testing and demonstration.
- **Daily granularity**: analysis is based on daily closing prices. Intraday analysis would require a paid Alpha Vantage tier.
