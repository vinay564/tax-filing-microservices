package com.uitax.payment.service.impl;

import java.math.BigDecimal;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.uitax.payment.entity.Payment;
import com.uitax.payment.entity.ProcessedEvent;
import com.uitax.payment.event.TaxFilingSubmittedEvent;
import com.uitax.payment.repository.PaymentRepository;
import com.uitax.payment.repository.ProcessedEventRepository;

@Component
public class PaymentEventListener {

    private static final Logger log = LoggerFactory.getLogger(PaymentEventListener.class);

    private final ProcessedEventRepository processedEventRepository;
    private final PaymentRepository paymentRepository;
    private final PaymentGatewayInvoker paymentGatewayInvoker;
    private final ObjectMapper objectMapper;

    public PaymentEventListener(ProcessedEventRepository processedEventRepository,
                                 PaymentRepository paymentRepository,
                                 PaymentGatewayInvoker paymentGatewayInvoker,
                                 ObjectMapper objectMapper) {
        this.processedEventRepository = processedEventRepository;
        this.paymentRepository = paymentRepository;
        this.paymentGatewayInvoker = paymentGatewayInvoker;
        this.objectMapper = objectMapper;
    }

    @KafkaListener(topics = "tax-filing-events", groupId = "payment-service-group")
    @Transactional
    public void handleTaxFilingSubmitted(String message) {

        TaxFilingSubmittedEvent event;
        try {
            event = objectMapper.readValue(message, TaxFilingSubmittedEvent.class);
        } catch (Exception e) {
            log.error("Failed to parse incoming message, skipping: {}", message, e);
            return;
        }

        UUID eventId = event.filingId();

        try {
            processedEventRepository.save(new ProcessedEvent(eventId));
        } catch (DataIntegrityViolationException e) {
            log.info("Event {} already processed, skipping duplicate.", eventId);
            return;
        }

        paymentGatewayInvoker.chargeGateway(event.filingId().toString());

        BigDecimal amountToCharge = event.taxDue();
        Payment payment = new Payment(event.filingId(), event.employerId(), amountToCharge);
        paymentRepository.save(payment);

        log.info("Processed payment {} for filing {}", payment.getId(), event.filingId());
    }

}