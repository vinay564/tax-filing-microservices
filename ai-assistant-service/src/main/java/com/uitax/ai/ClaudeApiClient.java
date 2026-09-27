package com.uitax.ai;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

@Slf4j
@Component
public class ClaudeApiClient {

    @Value("${claude.api.key}")
    private String apiKey;

    @Value("${claude.api.url}")
    private String apiUrl;

    @Value("${claude.api.model}")
    private String model;

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    public ClaudeApiClient(RestTemplate restTemplate, ObjectMapper objectMapper) {
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
    }

    public String chat(String message) throws Exception {
        try {
            // Build request payload
            ObjectNode request = objectMapper.createObjectNode();
            request.put("model", model);
            request.put("max_tokens", 1024);
            
            // Add message
            ArrayNode messages = request.putArray("messages");
            ObjectNode userMessage = messages.addObject();
            userMessage.put("role", "user");
            userMessage.put("content", message);

            // Set headers
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("x-api-key", apiKey);
            headers.set("anthropic-version", "2023-06-01");

            HttpEntity<String> entity = new HttpEntity<>(objectMapper.writeValueAsString(request), headers);

            // Call Claude API
            String response = restTemplate.postForObject(apiUrl, entity, String.class);
            
            // Parse response
            return parseResponse(response);

        } catch (RestClientException e) {
            log.error("Error calling Claude API: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to call Claude API: " + e.getMessage(), e);
        }
    }

    private String parseResponse(String response) throws Exception {
        JsonNode root = objectMapper.readTree(response);
        
        // Extract text from response
        if (root.has("content") && root.get("content").isArray() && root.get("content").size() > 0) {
            JsonNode firstContent = root.get("content").get(0);
            if (firstContent.has("text")) {
                return firstContent.get("text").asText();
            }
        }
        
        throw new RuntimeException("Unexpected response format from Claude API");
    }
}