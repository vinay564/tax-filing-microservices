package com.uitax.taxfiling.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record TaxFilingResponse(
        UUID id,
        UUID employerId,
        String filingPeriod,
        BigDecimal wagesReported,
        BigDecimal taxDue,
        String status
) {}