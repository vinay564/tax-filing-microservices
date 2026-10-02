package com.unemployment.agent;

import com.unemployment.model.RequestData;
import com.unemployment.model.AgentResponse;
import com.unemployment.agent.llm.LLMProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import java.util.logging.Logger;

@Component
public class EligibilityAgent implements Agent {
    
    private static final Logger logger = Logger.getLogger(EligibilityAgent.class.getName());
    private final LLMProvider llmProvider;

    @Autowired
    public EligibilityAgent(LLMProvider llmProvider) {
        this.llmProvider = llmProvider;
    }

    @Override
    public AgentResponse process(RequestData request) throws Exception {
        logger.info("EligibilityAgent processing request: " + request.getClaimantId());

        String systemPrompt = buildSystemPrompt();
        String userPrompt = buildUserPrompt(request);

        String decision = llmProvider.call(systemPrompt, userPrompt);
        logger.info("Claude response: " + decision);

        AgentResponse response = new AgentResponse(
            getAgentName(),
            extractDecision(decision),
            decision
        );

        return response;
    }

    @Override
    public String getAgentName() {
        return "EligibilityAgent";
    }

    @Override
    public boolean isAvailable() {
        return llmProvider != null;
    }

    private String buildSystemPrompt() {
        return "You are an unemployment benefits eligibility expert. Analyze claimant information and determine eligibility. " +
               "Respond with a JSON object containing 'decision' (ELIGIBLE/INELIGIBLE/PENDING_REVIEW) and 'reason'.";
    }

    private String buildUserPrompt(RequestData request) {
        return "Claimant ID: " + request.getClaimantId() + "\n" +
               "Claim Type: " + request.getClaimType() + "\n" +
               "Attributes: " + request.getAttributes().toString();
    }

    private String extractDecision(String response) {
        if (response.contains("ELIGIBLE")) return "ELIGIBLE";
        if (response.contains("INELIGIBLE")) return "INELIGIBLE";
        return "PENDING_REVIEW";
    }
}