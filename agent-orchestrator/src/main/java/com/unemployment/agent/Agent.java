package com.unemployment.agent;

import com.unemployment.model.RequestData;
import com.unemployment.model.AgentResponse;

public interface Agent {
    
    /**
     * Process a request and return an agent response
     */
    AgentResponse process(RequestData request) throws Exception;
    
    /**
     * Get the agent's name
     */
    String getAgentName();
    
    /**
     * Check if the agent is available
     */
    boolean isAvailable();
}