package com.uitax.payment.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.uitax.payment.entity.Payment;

public interface PaymentRepository extends JpaRepository<Payment, UUID> {

}