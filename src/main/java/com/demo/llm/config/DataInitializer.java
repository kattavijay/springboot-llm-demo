package com.demo.llm.config;

import com.demo.llm.rag.RagService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

/**
 * Seeds the in-memory knowledge base with sample documents on startup.
 * Remove or replace with real ingestion pipelines in production.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final RagService ragService;

    @Override
    public void run(String... args) {
        log.info("Seeding knowledge base with sample documents…");

        ragService.ingestDocument(
                "Spring AI Overview",
                """
                Spring AI is a Spring Framework project that provides abstractions and
                auto-configurations for AI/LLM providers such as OpenAI, Azure OpenAI,
                Hugging Face, and others. It includes ChatClient, EmbeddingClient,
                VectorStore, and higher-level chains for RAG workflows.
                """,
                "web");

        ragService.ingestDocument(
                "RAG Architecture Pattern",
                """
                Retrieval-Augmented Generation (RAG) is a technique that combines an LLM
                with a vector database. When a user asks a question, relevant document chunks
                are first retrieved via semantic similarity search (using embeddings), then
                injected into the LLM prompt as context. This reduces hallucinations and
                allows the model to answer questions about private or up-to-date knowledge.
                """,
                "pdf");

        ragService.ingestDocument(
                "Spring Boot Best Practices",
                """
                Spring Boot best practices include: using layered architecture (Controller →
                Service → Repository), externalising configuration via application.properties
                or Vault, enabling Actuator for health/metrics, using Spring Data JPA for
                data access, and structuring cross-cutting concerns (security, logging,
                validation) as separate beans or filters.
                """,
                "db");

        log.info("Knowledge base seeded with 3 documents.");
    }
}
