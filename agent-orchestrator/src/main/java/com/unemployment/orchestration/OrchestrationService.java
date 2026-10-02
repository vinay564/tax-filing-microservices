package com.unemployment.orchestration;

import com.unemployment.agent.Agent;
import com.unemployment.model.RequestData;
import com.unemployment.model.AgentResponse;
import org.springframework.stereotype.Service;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

@Service
public class OrchestrationService {
    
    private static final Logger logger = Logger.getLogger(OrchestrationService.class.getName());
    private final List<Agent> agents = new ArrayList<>();

    public void registerAgent(Agent agent) {
        agents.add(agent);
        logger.info("Registered agent: " + agent.getAgentName());
    }

    public List<AgentResponse> orchestrate(RequestData request) throws Exception {
        logger.info("Starting orchestration for request: " + request.getClaimantId());
        List<AgentResponse> responses = new ArrayList<>();

        for (Agent agent : agents) {
            if (agent.isAvailable()) {
                logger.info("Executing agent: " + agent.getAgentName());
                AgentResponse response = agent.process(request);
                responses.add(response);
                logger.info("Agent response: " + response.getDecision());
            }
        }

        logger.info("Orchestration completed with " + responses.size() + " responses");
        return responses;
    }

    public List<Agent> getRegisteredAgents() {
        return new ArrayList<>(agents);
    }
}