package com.unemployment.agent.llm;

public class OpenAIProvider implements LLMProvider {

    private final String apiKey;
    private final String model;

    public OpenAIProvider(String apiKey, String model) {
        this.apiKey = apiKey;
        this.model = model;
    }

    @Override
    public String call(String prompt) {
        return call("", prompt);
    }

    @Override
    public String call(String systemPrompt, String userPrompt) {
        // TODO: Implement OpenAI SDK integration
        return "{\"decision\": \"ELIGIBLE\", \"reason\": \"OpenAI provider not yet implemented\"}";
    }

    @Override
    public boolean isAvailable() {
        return apiKey != null && !apiKey.isEmpty();
    }

    @Override
    public String getProviderName() {
        return "OpenAI (" + model + ")";
    }
}