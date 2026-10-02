package com.unemployment.model;

import java.time.LocalDateTime;
import java.util.Map;

public class RequestData {
    private String claimantId;
    private String claimType;
    private Map<String, String> attributes;
    private LocalDateTime submittedAt;
    private String status;

    public RequestData() {}

    public RequestData(String claimantId, String claimType, Map<String, String> attributes) {
        this.claimantId = claimantId;
        this.claimType = claimType;
        this.attributes = attributes;
        this.submittedAt = LocalDateTime.now();
        this.status = "PENDING";
    }

    // Getters and Setters
    public String getClaimantId() { return claimantId; }
    public void setClaimantId(String claimantId) { this.claimantId = claimantId; }

    public String getClaimType() { return claimType; }
    public void setClaimType(String claimType) { this.claimType = claimType; }

    public Map<String, String> getAttributes() { return attributes; }
    public void setAttributes(Map<String, String> attributes) { this.attributes = attributes; }

    public LocalDateTime getSubmittedAt() { return submittedAt; }
    public void setSubmittedAt(LocalDateTime submittedAt) { this.submittedAt = submittedAt; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    @Override
    public String toString() {
        return "RequestData{" +
                "claimantId='" + claimantId + '\'' +
                ", claimType='" + claimType + '\'' +
                ", status='" + status + '\'' +
                '}';
    }
}