package com.uitax.payment.entity;

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
@Table(name = "payment")
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, unique = true)
    private UUID filingId;

    @Column(nullable = false)
    private UUID employerId;

    @Column(nullable = false)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentStatus status;

    protected Payment() {
        // required by JPA
    }

    public Payment(UUID filingId, UUID employerId, BigDecimal amount) {
        this.filingId = filingId;
        this.employerId = employerId;
        this.amount = amount;
        this.status = PaymentStatus.PROCESSED;
    }

    public UUID getId() { return id; }
    public UUID getFilingId() { return filingId; }
    public UUID getEmployerId() { return employerId; }
    public BigDecimal getAmount() { return amount; }
    public PaymentStatus getStatus() { return status; }

    public void markFailed() {
        this.status = PaymentStatus.FAILED;
    }
}