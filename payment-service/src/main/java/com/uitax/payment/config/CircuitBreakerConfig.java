package com.uitax.payment.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Configuration;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;

import jakarta.annotation.PostConstruct;

@Configuration
public class CircuitBreakerConfig {

    private static final Logger log = LoggerFactory.getLogger(CircuitBreakerConfig.class);

    private final CircuitBreakerRegistry circuitBreakerRegistry;

    public CircuitBreakerConfig(CircuitBreakerRegistry circuitBreakerRegistry) {
        this.circuitBreakerRegistry = circuitBreakerRegistry;
    }

    @PostConstruct
    public void registerStateChangeLogging() {
        CircuitBreaker circuitBreaker = circuitBreakerRegistry.circuitBreaker("paymentGateway");
        circuitBreaker.getEventPublisher()
                .onStateTransition(event ->
                        log.warn(">>> Circuit breaker 'paymentGateway' changed state: {} -> {}",
                                event.getStateTransition().getFromState(),
                                event.getStateTransition().getToState()));
    }
}