package com.fahmydiab.dqlgen;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface DocumentEmbeddingRepository extends JpaRepository<DocumentEmbedding, Long> {
    @Query(value = "SELECT * FROM document_embeddings ORDER BY embedding <=> :embedding LIMIT :limit", nativeQuery = true)
    List<DocumentEmbedding> findSimilarDocuments(@Param("embedding") float[] embedding, @Param("limit") int limit);
}
