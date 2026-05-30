package com.demo.llm.service;

import com.demo.llm.ai.AiOrchestrationService;
import com.demo.llm.model.KnowledgeDocument;
import com.demo.llm.rag.RagService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

/**
 * Service Layer – Business Logic & Workflow Orchestration.
 *
 * This is the layer between the REST controllers (API Layer) and the AI
 * Orchestration / Data Access layers. It is responsible for:
 *  - Input sanitisation and business rule enforcement
 *  - Workflow decisions (e.g. should we use RAG or direct LLM?)
 *  - Transaction boundaries
 *  - Mapping between DTOs and domain objects
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ChatService {

    private final AiOrchestrationService aiOrchestrationService;
    private final RagService ragService;

    // ── Single-turn Q&A ───────────────────────────────────────────────────────

    /**
     * Answers a one-shot question (no session memory).
     */
    public Map<String, String> askQuestion(String question) {
        validate(question);
        log.info("ask | question='{}'", truncate(question));
        String answer = aiOrchestrationService.ask(question);
        return Map.of("question", question, "answer", answer);
    }

    // ── Multi-turn chat ───────────────────────────────────────────────────────

    /**
     * Sends a message within a session (memory-aware).
     */
    @Transactional
    public Map<String, String> sendMessage(String sessionId, String message) {
        validate(message);
        if (sessionId == null || sessionId.isBlank()) {
            throw new IllegalArgumentException("sessionId must not be blank");
        }
        log.info("chat | session='{}' | message='{}'", sessionId, truncate(message));
        String reply = aiOrchestrationService.chat(sessionId, message);
        return Map.of("sessionId", sessionId, "reply", reply);
    }

    /**
     * Clears chat history for a session.
     */
    @Transactional
    public void clearSession(String sessionId) {
        aiOrchestrationService.clearSession(sessionId);
    }

    // ── Knowledge base management ─────────────────────────────────────────────

    /**
     * Ingests a document into the knowledge base (RAG).
     */
    @Transactional
    public KnowledgeDocument ingestDocument(String title, String content, String source) {
        if (title == null || title.isBlank()) throw new IllegalArgumentException("title required");
        if (content == null || content.isBlank()) throw new IllegalArgumentException("content required");
        log.info("ingest | title='{}' source='{}'", title, source);
        return ragService.ingestDocument(title, content, source);
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private void validate(String text) {
        if (text == null || text.isBlank()) {
            throw new IllegalArgumentException("Input text must not be blank");
        }
        if (text.length() > 4000) {
            throw new IllegalArgumentException("Input exceeds maximum length of 4000 characters");
        }
    }

    private String truncate(String s) {
        return s.length() > 80 ? s.substring(0, 80) + "…" : s;
    }
}
