package com.amraljundi.analyser.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.Executor;

import static java.util.concurrent.Executors.newVirtualThreadPerTaskExecutor;

@Configuration
public class AppConfig {

    @Bean(name = "virtualThreadExecutor")
    public Executor virtualThreadExecutor() {
        return newVirtualThreadPerTaskExecutor();
    }

}
