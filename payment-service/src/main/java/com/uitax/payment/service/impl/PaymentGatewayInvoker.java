package com.uitax.payment.service.impl;

import org.springframework.stereotype.Component;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;

@Component
public class PaymentGatewayInvoker {

    private final PaymentGatewayClient paymentGatewayClient;

    public PaymentGatewayInvoker(PaymentGatewayClient paymentGatewayClient) {
        this.paymentGatewayClient = paymentGatewayClient;
    }

    @CircuitBreaker(name = "paymentGateway", fallbackMethod = "gatewayFallback")
    public void chargeGateway(String filingId) {
        paymentGatewayClient.charge(filingId);
    }

    public void gatewayFallback(String filingId, Throwable t) {
        throw new RuntimeException("Payment gateway unavailable, cannot process filing " + filingId, t);
    }
}