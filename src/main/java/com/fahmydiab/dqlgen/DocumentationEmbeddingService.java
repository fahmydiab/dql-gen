package com.fahmydiab.dqlgen;

import com.pgvector.PGvector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class DocumentationEmbeddingService {
    private static final Logger logger = LoggerFactory.getLogger(DocumentationEmbeddingService.class);
    private final EmbeddingModel embeddingModel;
    private final DocumentationFetcherService fetcherService;
    private final DocumentEmbeddingRepository repository;

    public DocumentationEmbeddingService(EmbeddingModel embeddingModel, DocumentationFetcherService fetcherService, DocumentEmbeddingRepository repository, VectorStore vectorStore) {
        this.embeddingModel = embeddingModel;
        this.fetcherService = fetcherService;
        this.repository = repository;
    }

    public void embedDocumentation(String url) {
        logger.info("Starting embedding process for URL: {}", url);
        List<String> splits = fetcherService.fetchDocumentation(url);
        storeEmbeddingsInPgVector(splits);
        logger.info("Embedding process completed for URL: {}", url);
    }

    public void storeEmbeddingsInPgVector(List<String> texts) {
        if (texts.isEmpty()) {
            logger.info("No text chunks to embed.");
            return;
        }

        logger.info("Generating embeddings for {} text chunks.", texts.size());
        List<float[]> embeddingsList = embeddingModel.embed(texts);
        logger.info("Embeddings generated successfully.");

        List<DocumentEmbedding> documentEmbeddings = new ArrayList<>();
        for (int i = 0; i < texts.size(); i++) {
            float[] embedding = embeddingsList.get(i);
            DocumentEmbedding documentEmbedding = new DocumentEmbedding();
            documentEmbedding.setContent(texts.get(i));
            documentEmbedding.setEmbedding(new PGvector(embedding));
            documentEmbeddings.add(documentEmbedding);
        }
        logger.info("Saving {} embeddings to the database.", documentEmbeddings.size());
        repository.saveAll(documentEmbeddings);
        logger.info("Embeddings saved to pgvector via JPA and Spring AI.");
    }

}
