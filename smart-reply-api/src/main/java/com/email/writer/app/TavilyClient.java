package com.email.writer.app;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Map;

/** Fetches web context from Tavily to ground the generated reply. */
@Component
public class TavilyClient {

    private static final int MAX_QUERY_CHARS = 380; // Tavily rejects very long queries

    private final WebClient webClient;
    private final ObjectMapper mapper = new ObjectMapper();

    @Value("${tavily.api.url:https://api.tavily.com/search}")
    private String apiUrl;

    @Value("${tavily.api.key:}")
    private String apiKey;

    @Value("${tavily.max-results:3}")
    private int maxResults;

    public TavilyClient(WebClient.Builder builder) {
        this.webClient = builder.build();
    }

    public boolean isConfigured() {
        return !apiKey.isBlank();
    }

    /** Returns a short plain-text summary of search results, or "" if nothing useful. */
    public String search(String rawQuery) {
        if (!isConfigured() || rawQuery == null || rawQuery.isBlank()) {
            return "";
        }
        String query = rawQuery.replaceAll("\\s+", " ").trim();
        if (query.length() > MAX_QUERY_CHARS) {
            query = query.substring(0, MAX_QUERY_CHARS);
        }
        Map<String, Object> body = Map.of(
                "query", query,
                "search_depth", "basic",
                "max_results", maxResults,
                "include_answer", true
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
            StringBuilder out = new StringBuilder();
            String answer = root.path("answer").asText("");
            if (!answer.isBlank()) {
                out.append("Summary: ").append(answer).append("\n");
            }
            for (JsonNode r : root.path("results")) {
                out.append("- ").append(r.path("title").asText())
                        .append(": ").append(r.path("content").asText())
                        .append(" (").append(r.path("url").asText()).append(")\n");
            }
            return out.toString().trim();
        } catch (Exception e) {
            throw new IllegalStateException("Could not parse Tavily response: " + e.getMessage(), e);
        }
    }
}
