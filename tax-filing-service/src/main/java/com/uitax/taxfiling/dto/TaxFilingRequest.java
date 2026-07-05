package com.uitax.taxfiling.dto;

import java.math.BigDecimal;
import java.util.UUID;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

public record TaxFilingRequest(

        @NotNull(message = "employerId is required")
        UUID employerId,

        @NotBlank(message = "filingPeriod is required")
        String filingPeriod,

        @NotNull(message = "wagesReported is required")
        @PositiveOrZero(message = "wagesReported cannot be negative")
        BigDecimal wagesReported,

        @NotNull(message = "taxDue is required")
        @PositiveOrZero(message = "taxDue cannot be negative")
        BigDecimal taxDue

) {}