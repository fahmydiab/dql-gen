package com.fahmydiab.dqlgen;

import com.pgvector.PGvector;
import jakarta.persistence.*;
import org.hibernate.annotations.Type;

@Entity
@Table(name = "document_embeddings_v2")
public class DocumentEmbedding {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(columnDefinition = "TEXT")
    private String content;

    @Column(columnDefinition = "vector(768)")
    @Type(PGvectorType.class)
    private PGvector embedding;

    // Getters and setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public PGvector getEmbedding() {
        return embedding;
    }

    public void setEmbedding(PGvector embedding) {
        this.embedding = embedding;
    }
}
