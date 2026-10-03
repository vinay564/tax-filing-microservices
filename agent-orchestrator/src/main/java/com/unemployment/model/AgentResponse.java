package com.unemployment.model;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

public class AgentResponse {
    private String agentName;
    private String decision;
    private String reasoning;
    private Map<String, Object> metadata;
    private LocalDateTime processedAt;
    private boolean success;

    public AgentResponse(String agentName, String decision, String reasoning) {
        this.agentName = agentName;
        this.decision = decision;
        this.reasoning = reasoning;
        this.metadata = new HashMap<>();
        this.processedAt = LocalDateTime.now();
        this.success = true;
    }

    public String getAgentName() { return agentName; }
    public void setAgentName(String agentName) { this.agentName = agentName; }

    public String getDecision() { return decision; }
    public void setDecision(String decision) { this.decision = decision; }

    public String getReasoning() { return reasoning; }
    public void setReasoning(String reasoning) { this.reasoning = reasoning; }

    public Map<String, Object> getMetadata() { return metadata; }
    public void setMetadata(Map<String, Object> metadata) { this.metadata = metadata; }

    public LocalDateTime getProcessedAt() { return processedAt; }
    public void setProcessedAt(LocalDateTime processedAt) { this.processedAt = processedAt; }

    public boolean isSuccess() { return success; }
    public void setSuccess(boolean success) { this.success = success; }

    public void addMetadata(String key, Object value) {
        this.metadata.put(key, value);
    }

    @Override
    public String toString() {
        return "AgentResponse{" +
                "agentName='" + agentName + '\'' +
                ", decision='" + decision + '\'' +
                ", success=" + success +
                '}';
    }
}