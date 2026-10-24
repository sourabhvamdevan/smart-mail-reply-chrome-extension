package com.email.writer.app;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ReplyService {

    private static final Logger log = LoggerFactory.getLogger(ReplyService.class);

    private final List<LlmClient> clients;
    private final TavilyClient tavily;

    @Value("${llm.provider:groq}")
    private String defaultProvider;

    @Value("${tavily.default-enabled:false}")
    private boolean searchByDefault;

    public ReplyService(List<LlmClient> clients, TavilyClient tavily) {
        this.clients = clients;
        this.tavily = tavily;
    }

    public String generateReply(ReplyRequest request) {
        LlmClient client = pickClient(request.getProvider());

        String webContext = "";
        boolean useSearch = request.getUseSearch() != null ? request.getUseSearch() : searchByDefault;
        if (useSearch) {
            try {
                webContext = tavily.search(request.getEmailContent());
            } catch (Exception e) {
                // Search is a bonus; never fail the reply because of it.
                log.warn("Tavily search failed, continuing without web context: {}", e.getMessage());
            }
        }

        return client.generate(buildPrompt(request, webContext));
    }

    private LlmClient pickClient(String requested) {
        String wanted = (requested == null || requested.isBlank() ? defaultProvider : requested)
                .trim().toLowerCase();
        return clients.stream()
                .filter(c -> c.name().equals(wanted))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException(
                        "Unknown provider '" + wanted + "'. Use one of: "
                                + clients.stream().map(LlmClient::name).toList()));
    }

    private String buildPrompt(ReplyRequest request, String webContext) {
        StringBuilder prompt = new StringBuilder();
        prompt.append("Generate a professional email reply for the following email content. Please don't generate a subject line. ");
        if (request.getTone() != null && !request.getTone().isEmpty()) {
            prompt.append("Use a ").append(request.getTone()).append(" tone.");
        }
        if (!webContext.isBlank()) {
            prompt.append("\n\nUse this up-to-date web research only where it is relevant, ")
                    .append("and don't invent facts beyond it:\n").append(webContext);
        }
        prompt.append("\n\nOriginal email:\n").append(request.getEmailContent());
        return prompt.toString();
    }
}
