package com.uitax.payment.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.uitax.payment.entity.ProcessedEvent;

public interface ProcessedEventRepository extends JpaRepository<ProcessedEvent, UUID> {

}