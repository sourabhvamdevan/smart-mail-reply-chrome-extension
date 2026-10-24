package com.email.writer.app;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Map;

@Component
public class GeminiClient implements LlmClient {

    private final WebClient webClient;
    private final ObjectMapper mapper = new ObjectMapper();

    @Value("${gemini.api.url:}")
    private String apiUrl;

    @Value("${gemini.api.key:}")
    private String apiKey;

    public GeminiClient(WebClient.Builder builder) {
        this.webClient = builder.build();
    }

    @Override
    public String name() {
        return "gemini";
    }

    @Override
    public String generate(String prompt) {
        if (apiUrl.isBlank() || apiKey.isBlank()) {
            throw new IllegalStateException("Gemini is not configured (set GEMINI_URL and GEMINI_KEY)");
        }
        Map<String, Object> body = Map.of(
                "contents", new Object[]{
                        Map.of("parts", new Object[]{Map.of("text", prompt)})
                }
        );
        String response = webClient.post()
                .uri(apiUrl + apiKey)
                .header("Content-Type", "application/json")
                .bodyValue(body)
                .retrieve()
                .bodyToMono(String.class)
                .block();
        try {
            JsonNode root = mapper.readTree(response);
            return root.path("candidates").get(0)
                    .path("content").path("parts").get(0)
                    .path("text").asText();
        } catch (Exception e) {
            throw new IllegalStateException("Could not parse Gemini response: " + e.getMessage(), e);
        }
    }
}
