package com.unemployment.agent.llm;

public interface LLMProvider {
    String call(String prompt);
    String call(String systemPrompt, String userPrompt);
    boolean isAvailable();
    String getProviderName();
}