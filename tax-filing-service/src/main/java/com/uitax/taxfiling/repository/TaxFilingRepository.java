package com.uitax.taxfiling.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.uitax.taxfiling.entity.TaxFiling;

public interface TaxFilingRepository extends JpaRepository<TaxFiling, UUID> {

}