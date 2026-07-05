package com.uitax.taxfiling.controller;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.uitax.taxfiling.dto.TaxFilingRequest;
import com.uitax.taxfiling.dto.TaxFilingResponse;
import com.uitax.taxfiling.service.TaxFilingService;

@RestController
@RequestMapping("/api/filings")
public class TaxFilingController {

    private final TaxFilingService taxFilingService;

    public TaxFilingController(TaxFilingService taxFilingService) {
        this.taxFilingService = taxFilingService;
    }

    @PostMapping
    public ResponseEntity<TaxFilingResponse> submitFiling(@Valid @RequestBody TaxFilingRequest request) {
        TaxFilingResponse response = taxFilingService.submitFiling(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

}