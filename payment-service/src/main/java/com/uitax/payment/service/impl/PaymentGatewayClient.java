package com.uitax.payment.service.impl;

import java.util.concurrent.ThreadLocalRandom;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class PaymentGatewayClient {

    private static final Logger log = LoggerFactory.getLogger(PaymentGatewayClient.class);

    public String charge(String filingId) {
        log.info("Calling external payment gateway for filing {}...", filingId);

        if (ThreadLocalRandom.current().nextInt(100) < 90) {
            log.error("Payment gateway call FAILED for filing {}", filingId);
            throw new RuntimeException("Payment gateway unavailable");
        }

        log.info("Payment gateway call SUCCEEDED for filing {}", filingId);
        return "GATEWAY-OK";
    }
}