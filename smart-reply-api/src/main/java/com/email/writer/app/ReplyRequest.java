package com.email.writer.app;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReplyRequest {
    private String emailContent;
    private String tone;
    /** Optional: "groq" or "gemini". Falls back to llm.provider when empty. */
    private String provider;
    /** Optional: enrich the reply with Tavily web search. Falls back to tavily.default-enabled. */
    private Boolean useSearch;
}
