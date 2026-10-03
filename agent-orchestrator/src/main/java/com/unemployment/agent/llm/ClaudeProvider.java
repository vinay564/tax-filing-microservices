package com.unemployment.agent.llm;

import com.anthropic.client.AnthropicClient;
import com.anthropic.client.okhttp.AnthropicOkHttpClient;
import com.anthropic.models.messages.Message;
import com.anthropic.models.messages.MessageCreateParams;
import com.anthropic.models.messages.MessageParam;
import com.anthropic.models.messages.TextBlock;
import java.util.ArrayList;
import java.util.List;

public class ClaudeProvider implements LLMProvider {

    private final AnthropicClient client;
    private final String model;

    public ClaudeProvider(String apiKey, String model) {
        this.client = AnthropicOkHttpClient.builder()
            .apiKey(apiKey)
            .build();
        this.model = model;
    }

    @Override
    public String call(String prompt) {
        return call("", prompt);
    }

    @Override
    public String call(String systemPrompt, String userPrompt) {
        MessageCreateParams.Builder paramsBuilder = MessageCreateParams.builder()
            .model(model)
            .maxTokens(1024)
            .addUserMessage(userPrompt);

        if (systemPrompt != null && !systemPrompt.isEmpty()) {
            paramsBuilder.system(systemPrompt);
        }

        MessageCreateParams params = paramsBuilder.build();

        Message message = client.messages().create(params);
        
        return message.content()
        	    .stream()
        	    .map(Object::toString)
        	    .findFirst()
        	    .orElse("");
    }

    @Override
    public boolean isAvailable() {
        return client != null;
    }

    @Override
    public String getProviderName() {
        return "Claude (" + model + ")";
    }
}