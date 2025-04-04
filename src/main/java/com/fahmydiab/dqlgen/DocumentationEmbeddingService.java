package com.fahmydiab.dqlgen;

import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class DocumentationEmbeddingService {
    private final EmbeddingModel embeddingModel;
    private final DocumentationFetcherService fetcherService;
    private final DocumentEmbeddingRepository repository;

    public DocumentationEmbeddingService(EmbeddingModel embeddingModel, DocumentationFetcherService fetcherService, DocumentEmbeddingRepository repository) {
        this.embeddingModel = embeddingModel;
        this.fetcherService = fetcherService;
        this.repository = repository;
    }

    public void embedDocumentation(String url) {
        String content = fetcherService.fetchDocumentation(url);
        // EmbeddingModel.embed returns float[]
        float[] embeddingArray = embeddingModel.embed(content);

        // Convert float[] to List<Float>
        List<Float> embeddingList = new ArrayList<>(embeddingArray.length);
        for (float value : embeddingArray) {
            embeddingList.add(value);
        }

        DocumentEmbedding documentEmbedding = new DocumentEmbedding();
        documentEmbedding.setContent(content);
        documentEmbedding.setEmbedding(embeddingList);

        repository.save(documentEmbedding);
    }
}
