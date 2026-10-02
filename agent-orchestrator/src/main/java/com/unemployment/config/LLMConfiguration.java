package com.unemployment.config;

import com.unemployment.agent.llm.ClaudeProvider;
import com.unemployment.agent.llm.GeminiProvider;
import com.unemployment.agent.llm.LLMProvider;
import com.unemployment.agent.llm.OpenAIProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class LLMConfiguration {

    @Value("${llm.provider}")
    private String provider;

    @Value("${llm.api-key}")
    private String apiKey;

    @Value("${llm.model}")
    private String model;

    @Bean
    public LLMProvider llmProvider() {
        switch (provider.toLowerCase()) {
            case "claude":
                return new ClaudeProvider(apiKey, model);
            case "openai":
                return new OpenAIProvider(apiKey, model);
            case "gemini":
                return new GeminiProvider(apiKey, model);
            default:
                throw new IllegalArgumentException("Unknown LLM provider: " + provider);
        }
    }
}