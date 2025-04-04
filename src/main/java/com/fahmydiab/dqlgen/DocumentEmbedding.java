package com.fahmydiab.dqlgen;

import jakarta.persistence.*;
import java.util.List;

@Entity
@Table(name = "document_embeddings")
public class DocumentEmbedding {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(columnDefinition = "TEXT")
    private String content;

    @Column(columnDefinition = "vector(1024)")
    @Convert(converter = EmbeddingVectorConverter.class)
    private List<Float> embedding;

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

    public List<Float> getEmbedding() {
        return embedding;
    }

    public void setEmbedding(List<Float> embedding) {
        this.embedding = embedding;
    }
}
