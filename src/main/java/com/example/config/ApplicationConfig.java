package com.example.config;

import java.time.Clock;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ApplicationConfig {

    @Bean
    Clock clock() {
        return Clock.systemUTC();
    }

    @Bean(destroyMethod = "close")
    Executor auditExecutor() {
        return Executors.newVirtualThreadPerTaskExecutor();
    }
}
