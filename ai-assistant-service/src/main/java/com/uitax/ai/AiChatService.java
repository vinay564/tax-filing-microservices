package com.uitax.ai;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class AiChatService {

    private final ClaudeApiClient claudeApiClient;

    public AiChatService(ClaudeApiClient claudeApiClient) {
        this.claudeApiClient = claudeApiClient;
    }

    public ChatResponse chat(String message) {
        try {
            // Validate input
            if (message == null || message.trim().isEmpty()) {
                return new ChatResponse(null, "Message cannot be empty");
            }

            // Call Claude API
            String reply = claudeApiClient.chat(message);
            
            log.info("Successfully received response from Claude API");
            return new ChatResponse(reply, null);

        } catch (Exception e) {
            log.error("Error in chat service: {}", e.getMessage(), e);
            return new ChatResponse(null, "Error: " + e.getMessage());
        }
    }
}