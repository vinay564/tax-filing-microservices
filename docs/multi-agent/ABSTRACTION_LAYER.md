# Abstraction Layer: AI Provider

## Why Abstraction?

Not locked to Claude. Can swap Claude → OpenAI → Gemini without changing agent code.

---

## LLMProvider Interface

```java
package com.unemployment.agent.llm;

public interface LLMProvider {
    String call(String prompt);
    String call(String systemPrompt, String userPrompt);
    boolean isAvailable();
    String getProviderName();
}
```

---

## Implementation: Claude

```java
package com.unemployment.agent.llm;

public class ClaudeProvider implements LLMProvider {
    private final Anthropic client;
    private final String model;
    
    public ClaudeProvider(String apiKey, String model) {
        this.client = new Anthropic.Builder().apiKey(apiKey).build();
        this.model = model;
    }
    
    @Override
    public String call(String prompt) {
        return call("", prompt);
    }
    
    @Override
    public String call(String systemPrompt, String userPrompt) {
        Message message = client.messages().create(
            MessageCreateParams.builder()
                .model(model)
                .maxTokens(1024)
                .systemPrompt(systemPrompt)
                .messages(List.of(
                    MessageParam.builder()
                        .role(MessageParam.Role.USER)
                        .content(userPrompt)
                        .build()
                ))
                .build()
        );
        return message.getContent().get(0).getText();
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
```

---

## Implementation: OpenAI

```java
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
        // OpenAI API call with gpt-4 or gpt-3.5-turbo
        return "OpenAI response";
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
```

---

## Implementation: Gemini

```java
package com.unemployment.agent.llm;

public class GeminiProvider implements LLMProvider {
    private final String projectId;
    private final String model;
    
    public GeminiProvider(String projectId, String model) {
        this.projectId = projectId;
        this.model = model;
    }
    
    @Override
    public String call(String prompt) {
        return call("", prompt);
    }
    
    @Override
    public String call(String systemPrompt, String userPrompt) {
        // Gemini API call
        return "Gemini response";
    }
    
    @Override
    public boolean isAvailable() {
        return projectId != null && !projectId.isEmpty();
    }
    
    @Override
    public String getProviderName() {
        return "Gemini (" + model + ")";
    }
}
```

---

## Spring Configuration

```java
@Configuration
public class LLMConfiguration {
    
    @Bean
    public LLMProvider llmProvider(
        @Value("${llm.provider}") String provider,
        @Value("${llm.api-key:}") String apiKey,
        @Value("${llm.project-id:}") String projectId,
        @Value("${llm.model}") String model) {
        
        return switch(provider.toLowerCase()) {
            case "claude" -> new ClaudeProvider(apiKey, model);
            case "openai" -> new OpenAIProvider(apiKey, model);
            case "gemini" -> new GeminiProvider(projectId, model);
            default -> throw new IllegalArgumentException("Unknown provider: " + provider);
        };
    }
}
```

---

## Configuration (application.yml)

```yaml
llm:
  provider: claude
  api-key: ${CLAUDE_API_KEY}
  model: claude-opus
```

---

## Benefits

✅ Not locked to Claude
✅ Easy to test with mocks
✅ Easy to add OpenAI/Gemini later
✅ Switch providers at runtime via config
✅ Agents don't know which provider they use
✅ Enterprise-ready pattern
