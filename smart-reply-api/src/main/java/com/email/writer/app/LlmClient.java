package com.email.writer.app;

/** Common contract for every LLM provider (Gemini, Groq, ...). */
public interface LlmClient {

    /** Lower-case provider id used in config and in the request, e.g. "groq". */
    String name();

    /** Sends the prompt and returns the generated text. */
    String generate(String prompt);
}
