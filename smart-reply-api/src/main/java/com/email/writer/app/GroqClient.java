package com.email.writer.app;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;
import java.util.Map;

/** Groq exposes an OpenAI-compatible chat completions endpoint. */
@Component
public class GroqClient implements LlmClient {

    private final WebClient webClient;
    private final ObjectMapper mapper = new ObjectMapper();

    @Value("${groq.api.url:https://api.groq.com/openai/v1/chat/completions}")
    private String apiUrl;

    @Value("${groq.api.key:}")
    private String apiKey;

    @Value("${groq.model:llama-3.3-70b-versatile}")
    private String model;

    @Value("${groq.temperature:0.7}")
    private double temperature;

    public GroqClient(WebClient.Builder builder) {
        this.webClient = builder.build();
    }

    @Override
    public String name() {
        return "groq";
    }

    @Override
    public String generate(String prompt) {
        if (apiKey.isBlank()) {
            throw new IllegalStateException("Groq is not configured (set GROQ_API_KEY)");
        }
        Map<String, Object> body = Map.of(
                "model", model,
                "temperature", temperature,
                "messages", List.of(
                        Map.of("role", "system",
                                "content", "You write clear, polished email replies. Output only the reply body."),
                        Map.of("role", "user", "content", prompt)
                )
        );
        String response = webClient.post()
                .uri(apiUrl)
                .header("Authorization", "Bearer " + apiKey)
                .header("Content-Type", "application/json")
                .bodyValue(body)
                .retrieve()
                .bodyToMono(String.class)
                .block();
        try {
            JsonNode root = mapper.readTree(response);
            return root.path("choices").get(0).path("message").path("content").asText();
        } catch (Exception e) {
            throw new IllegalStateException("Could not parse Groq response: " + e.getMessage(), e);
        }
    }
}
