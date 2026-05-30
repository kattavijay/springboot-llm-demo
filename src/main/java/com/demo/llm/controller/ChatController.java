package com.demo.llm.controller;

import com.demo.llm.model.KnowledgeDocument;
import com.demo.llm.service.ChatService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * API Layer – REST Controllers / Endpoints.
 *
 * Exposes three groups of endpoints:
 *   POST /api/ask              → single-turn Q&A
 *   POST /api/chat/{sessionId} → multi-turn conversation (with memory)
 *   POST /api/documents        → knowledge-base ingestion (RAG)
 *
 * Security, validation, and logging are applied at this layer.
 */
@Slf4j
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class ChatController {

    private final ChatService chatService;

    // ── ① Single-turn Q&A ─────────────────────────────────────────────────────

    /**
     * POST /api/ask
     * Body: { "question": "What is Spring AI?" }
     */
    @PostMapping("/ask")
    public ResponseEntity<Map<String, String>> ask(@Valid @RequestBody AskRequest req) {
        Map<String, String> result = chatService.askQuestion(req.question());
        return ResponseEntity.ok(result);
    }

    // ── ② Multi-turn chat (with memory) ───────────────────────────────────────

    /**
     * POST /api/chat/{sessionId}
     * Body: { "message": "Tell me more about that" }
     */
    @PostMapping("/chat/{sessionId}")
    public ResponseEntity<Map<String, String>> chat(
            @PathVariable String sessionId,
            @Valid @RequestBody ChatRequest req) {
        Map<String, String> result = chatService.sendMessage(sessionId, req.message());
        return ResponseEntity.ok(result);
    }

    /**
     * DELETE /api/chat/{sessionId}  – clears conversation memory
     */
    @DeleteMapping("/chat/{sessionId}")
    public ResponseEntity<Void> clearSession(@PathVariable String sessionId) {
        chatService.clearSession(sessionId);
        return ResponseEntity.noContent().build();
    }

    // ── ③ Knowledge base / RAG ingestion ──────────────────────────────────────

    /**
     * POST /api/documents
     * Body: { "title": "...", "content": "...", "source": "pdf" }
     */
    @PostMapping("/documents")
    public ResponseEntity<KnowledgeDocument> ingestDocument(
            @Valid @RequestBody IngestRequest req) {
        KnowledgeDocument doc = chatService.ingestDocument(req.title(), req.content(), req.source());
        return ResponseEntity.ok(doc);
    }

    // ── Request / Response records ─────────────────────────────────────────────

    record AskRequest(
            @NotBlank @Size(max = 4000) String question) {}

    record ChatRequest(
            @NotBlank @Size(max = 4000) String message) {}

    record IngestRequest(
            @NotBlank String title,
            @NotBlank @Size(max = 100_000) String content,
            String source) {}
}
