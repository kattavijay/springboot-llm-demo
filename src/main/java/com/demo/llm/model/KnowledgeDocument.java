package com.demo.llm.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * A document chunk stored in the knowledge base for RAG retrieval.
 * In production, the embedding would live in pgvector; here we keep it simple.
 */
@Entity
@Table(name = "knowledge_documents")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class KnowledgeDocument {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String content;

    @Column(nullable = false)
    private String source;        // "pdf", "web", "api", "db"

    private LocalDateTime ingestedAt;

    @PrePersist
    protected void onCreate() {
        ingestedAt = LocalDateTime.now();
    }
}
