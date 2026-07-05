package com.uitax.taxfiling.service.impl;

import java.time.Instant;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.uitax.taxfiling.dto.TaxFilingRequest;
import com.uitax.taxfiling.dto.TaxFilingResponse;
import com.uitax.taxfiling.entity.OutboxEvent;
import com.uitax.taxfiling.entity.TaxFiling;
import com.uitax.taxfiling.event.TaxFilingSubmittedEvent;
import com.uitax.taxfiling.repository.OutboxEventRepository;
import com.uitax.taxfiling.repository.TaxFilingRepository;
import com.uitax.taxfiling.service.TaxFilingService;

@Service
public class TaxFilingServiceImpl implements TaxFilingService {

    private final TaxFilingRepository taxFilingRepository;
    private final OutboxEventRepository outboxEventRepository;
    private final ObjectMapper objectMapper;

    public TaxFilingServiceImpl(TaxFilingRepository taxFilingRepository,
                                 OutboxEventRepository outboxEventRepository,
                                 ObjectMapper objectMapper) {
        this.taxFilingRepository = taxFilingRepository;
        this.outboxEventRepository = outboxEventRepository;
        this.objectMapper = objectMapper;
    }

    @Override
    @Transactional
    public TaxFilingResponse submitFiling(TaxFilingRequest request) {

        TaxFiling filing = new TaxFiling(
                request.employerId(),
                request.filingPeriod(),
                request.wagesReported(),
                request.taxDue());

        taxFilingRepository.save(filing);

        TaxFilingSubmittedEvent eventPayload = new TaxFilingSubmittedEvent(
                filing.getId(),
                filing.getEmployerId(),
                filing.getFilingPeriod(),
                filing.getTaxDue(),
                Instant.now());

        OutboxEvent outboxEvent = new OutboxEvent(
                "TaxFiling",
                filing.getId(),
                "TaxFilingSubmitted",
                toJson(eventPayload));

        outboxEventRepository.save(outboxEvent);

        return new TaxFilingResponse(
                filing.getId(),
                filing.getEmployerId(),
                filing.getFilingPeriod(),
                filing.getWagesReported(),
                filing.getTaxDue(),
                filing.getStatus().name());
    }

    private String toJson(Object payload) {
        try {
            return objectMapper.writeValueAsString(payload);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Failed to serialize event payload for outbox", e);
        }
    }

}