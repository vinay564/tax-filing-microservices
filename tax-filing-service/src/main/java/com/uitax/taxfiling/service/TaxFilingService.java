package com.uitax.taxfiling.service;

import com.uitax.taxfiling.dto.TaxFilingRequest;
import com.uitax.taxfiling.dto.TaxFilingResponse;

public interface TaxFilingService {

    TaxFilingResponse submitFiling(TaxFilingRequest request);

}