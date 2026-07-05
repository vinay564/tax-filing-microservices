package com.uitax.taxfiling.event;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record TaxFilingSubmittedEvent(
        UUID filingId,
        UUID employerId,
        String filingPeriod,
        BigDecimal taxDue,
        Instant occurredAt
) {}