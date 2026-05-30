package com.demo.llm.ai;

import org.springframework.stereotype.Component;

/**
 * AI Orchestration Layer – Prompt Management.
 * Centralises all system prompts and prompt templates so they are
 * easy to version, test, and swap without touching business logic.
 */
@Component
public class PromptManager {

    /** Base system prompt injected into every chat request. */
    public static final String SYSTEM_PROMPT = """
            You are a helpful enterprise assistant built on Spring Boot and Spring AI.
            You answer questions clearly and concisely.
            When context is provided from the knowledge base, use it to ground your answer.
            If you are unsure, say so — do not fabricate information.
            """;

    /** RAG-augmented prompt template. {context} and {question} are filled at runtime. */
    public static final String RAG_PROMPT_TEMPLATE = """
            Use the following context retrieved from the knowledge base to answer the question.
            If the context does not contain the answer, rely on your general knowledge but say so.
            
            Context:
            {context}
            
            Question: {question}
            """;

    /**
     * Builds a RAG prompt by injecting retrieved context.
     */
    public String buildRagPrompt(String context, String question) {
        return RAG_PROMPT_TEMPLATE
                .replace("{context}", context)
                .replace("{question}", question);
    }
}
