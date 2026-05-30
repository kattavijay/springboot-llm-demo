package com.demo.llm.rag;

import com.demo.llm.model.KnowledgeDocument;
import com.demo.llm.repository.KnowledgeDocumentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * AI Orchestration Layer – RAG (Retrieval-Augmented Generation).
 *
 * Flow (step ② in the architecture diagram):
 *   1. Receive user question
 *   2. Extract keywords / embed the question
 *   3. Query the Vector DB / Knowledge Base
 *   4. Return retrieved text chunks as context for the LLM prompt
 *
 * Production upgrade path:
 *   Replace the keyword search with Spring AI's VectorStore:
 *     List<Document> docs = vectorStore.similaritySearch(
 *         SearchRequest.query(question).withTopK(4));
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RagService {

    private final KnowledgeDocumentRepository documentRepository;

    /**
     * Retrieves relevant document chunks for the given question.
     * @return concatenated context string, or empty string if nothing found
     */
    public String retrieveContext(String question) {
        // Simple keyword extraction – replace with embedding similarity in prod
        String keyword = extractPrimaryKeyword(question);
        List<KnowledgeDocument> docs = documentRepository.searchByKeyword(keyword);

        if (docs.isEmpty()) {
            log.debug("RAG: no documents found for keyword '{}'", keyword);
            return "";
        }

        log.debug("RAG: found {} document(s) for keyword '{}'", docs.size(), keyword);
        return docs.stream()
                .limit(3)   // top-3 chunks
                .map(d -> "[" + d.getTitle() + "]\n" + d.getContent())
                .collect(Collectors.joining("\n\n---\n\n"));
    }

    /**
     * Ingests a document into the knowledge base.
     * In production: also generate and store an embedding via
     * vectorStore.add(List.of(new Document(content)));
     */
    public KnowledgeDocument ingestDocument(String title, String content, String source) {
        KnowledgeDocument doc = KnowledgeDocument.builder()
                .title(title)
                .content(content)
                .source(source)
                .build();
        KnowledgeDocument saved = documentRepository.save(doc);
        log.info("Ingested document id={} title='{}'", saved.getId(), saved.getTitle());
        return saved;
    }

    // ── Helpers ──────────────────────────────────────────────────────────────

    private String extractPrimaryKeyword(String question) {
        // Naive: take the longest word (> 4 chars) that is not a stop word.
        // Replace with NLP tokenization or embedding-based retrieval in prod.
        String[] stopWords = {"what", "which", "where", "when", "does", "have",
                               "about", "tell", "explain", "describe", "list"};
        return java.util.Arrays.stream(question.toLowerCase().split("\\W+"))
                .filter(w -> w.length() > 4)
                .filter(w -> java.util.Arrays.stream(stopWords).noneMatch(w::equals))
                .findFirst()
                .orElse(question);
    }
}
