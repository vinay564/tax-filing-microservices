package com.uitax.taxfiling.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "tax_filing")
public class TaxFiling extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private UUID employerId;

    @Column(nullable = false)
    private String filingPeriod;

    @Column(nullable = false)
    private BigDecimal wagesReported;

    @Column(nullable = false)
    private BigDecimal taxDue;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private FilingStatus status;

    protected TaxFiling() {
        // required by JPA
    }

    public TaxFiling(UUID employerId, String filingPeriod, BigDecimal wagesReported, BigDecimal taxDue) {
        this.employerId = employerId;
        this.filingPeriod = filingPeriod;
        this.wagesReported = wagesReported;
        this.taxDue = taxDue;
        this.status = FilingStatus.SUBMITTED;
    }

    public UUID getId() { return id; }
    public UUID getEmployerId() { return employerId; }
    public String getFilingPeriod() { return filingPeriod; }
    public BigDecimal getWagesReported() { return wagesReported; }
    public BigDecimal getTaxDue() { return taxDue; }
    public FilingStatus getStatus() { return status; }

    public void markProcessed() {
        this.status = FilingStatus.PROCESSED;
    }

    public void markFailed() {
        this.status = FilingStatus.FAILED;
    }
}