CREATE TABLE market_analysis
(
    id             UUID           PRIMARY KEY DEFAULT gen_random_uuid(),
    symbol         VARCHAR(10)    NOT NULL,
    direction      VARCHAR(10)    NOT NULL,
    percent_change DECIMAL(10, 2) NOT NULL,
    analyzed_at    DATE           NOT NULL,
    UNIQUE (symbol, analyzed_at)
);

CREATE INDEX idx_market_analysis_symbol_analyzed_at
    ON market_analysis (symbol, analyzed_at);

CREATE TABLE job_status
(
    symbol       VARCHAR(10) NOT NULL PRIMARY KEY,
    last_fetched DATE        NOT NULL
);

CREATE TABLE shedlock
(
    name       VARCHAR(64)  NOT NULL PRIMARY KEY,
    lock_until TIMESTAMP    NOT NULL,
    locked_at  TIMESTAMP    NOT NULL,
    locked_by  VARCHAR(255) NOT NULL
);