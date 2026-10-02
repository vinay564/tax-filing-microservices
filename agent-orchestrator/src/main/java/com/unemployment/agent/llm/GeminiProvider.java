package com.unemployment.agent.llm;

public class GeminiProvider implements LLMProvider {

    private final String apiKey;
    private final String model;

    public GeminiProvider(String apiKey, String model) {
        this.apiKey = apiKey;
        this.model = model;
    }

    @Override
    public String call(String prompt) {
        return call("", prompt);
    }

    @Override
    public String call(String systemPrompt, String userPrompt) {
        // TODO: Implement Gemini SDK integration
        return "{\"decision\": \"ELIGIBLE\", \"reason\": \"Gemini provider not yet implemented\"}";
    }

    @Override
    public boolean isAvailable() {
        return apiKey != null && !apiKey.isEmpty();
    }

    @Override
    public String getProviderName() {
        return "Gemini (" + model + ")";
    }
}