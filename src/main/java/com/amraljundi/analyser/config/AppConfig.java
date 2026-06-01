package com.amraljundi.analyser.config;

import com.amraljundi.analyser.exception.StockDataJobException;
import com.amraljundi.analyser.job.StockDataJob;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.util.concurrent.Executor;

import static java.util.concurrent.Executors.newVirtualThreadPerTaskExecutor;

@Configuration
@EnableScheduling
@EnableConfigurationProperties(JobConfig.class)
public class AppConfig {
    private static final Logger log = LoggerFactory.getLogger(AppConfig.class);

    @Bean(name = "virtualThreadExecutor")
    public Executor virtualThreadExecutor() {
        return newVirtualThreadPerTaskExecutor();
    }

    @Bean
    @Profile("dev")
    public CommandLineRunner triggerJob(StockDataJob job) {
        return args -> {
            try {
                job.run();
            } catch (StockDataJobException e) {
                log.warn("Job failed on startup: {}", e.getMessage());
            }
        };
    }

}
