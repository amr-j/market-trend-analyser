package com.amraljundi.analyser.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

@ConfigurationProperties(prefix = "job")
public record JobConfig(
        List<String> symbols,
        int lookbackDays
) {
}
