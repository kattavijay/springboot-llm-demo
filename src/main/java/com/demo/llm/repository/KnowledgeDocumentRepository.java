package com.demo.llm.repository;

import com.demo.llm.model.KnowledgeDocument;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Data Access Layer – Knowledge base document storage.
 * In production, use Spring AI's VectorStore (pgvector / Pinecone) for
 * semantic similarity search. This demo uses a basic keyword search.
 */
@Repository
public interface KnowledgeDocumentRepository extends JpaRepository<KnowledgeDocument, Long> {

    @Query("SELECT d FROM KnowledgeDocument d WHERE " +
           "LOWER(d.content) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(d.title)   LIKE LOWER(CONCAT('%', :keyword, '%'))")
    List<KnowledgeDocument> searchByKeyword(@Param("keyword") String keyword);
}
