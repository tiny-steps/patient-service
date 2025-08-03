package com.tintsteps.patientservice.config;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryRegistry;
import io.github.resilience4j.timelimiter.TimeLimiter;
import io.github.resilience4j.timelimiter.TimeLimiterRegistry;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration for Resilience4j components used in external service integrations
 */
@Configuration
public class ResilienceConfig {

    // User Service Resilience Components
    @Bean
    public CircuitBreaker userServiceCircuitBreaker(CircuitBreakerRegistry registry) {
        return registry.circuitBreaker("ts-user-service");
    }

    @Bean
    public Retry userServiceRetry(RetryRegistry registry) {
        return registry.retry("ts-user-service");
    }

    @Bean
    public TimeLimiter userServiceTimeLimiter(TimeLimiterRegistry registry) {
        return registry.timeLimiter("ts-user-service");
    }

    // Address Service Resilience Components
    @Bean
    public CircuitBreaker addressServiceCircuitBreaker(CircuitBreakerRegistry registry) {
        return registry.circuitBreaker("ts-address-service");
    }

    @Bean
    public Retry addressServiceRetry(RetryRegistry registry) {
        return registry.retry("ts-address-service");
    }

    @Bean
    public TimeLimiter addressServiceTimeLimiter(TimeLimiterRegistry registry) {
        return registry.timeLimiter("ts-address-service");
    }

    // Schedule Service Resilience Components
    @Bean
    public CircuitBreaker scheduleServiceCircuitBreaker(CircuitBreakerRegistry registry) {
        return registry.circuitBreaker("ts-schedule-service");
    }

    @Bean
    public Retry scheduleServiceRetry(RetryRegistry registry) {
        return registry.retry("ts-schedule-service");
    }

    @Bean
    public TimeLimiter scheduleServiceTimeLimiter(TimeLimiterRegistry registry) {
        return registry.timeLimiter("ts-schedule-service");
    }
}
