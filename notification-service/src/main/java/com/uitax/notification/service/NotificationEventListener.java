package com.uitax.notification.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class NotificationEventListener {

    private static final Logger log = LoggerFactory.getLogger(NotificationEventListener.class);

    @KafkaListener(topics = "tax-filing-events", groupId = "notification-service-group")
    public void handleTaxFilingSubmitted(String message) {
        log.info("📩 NOTIFICATION: Tax filing event received: {}", message);
    }

}