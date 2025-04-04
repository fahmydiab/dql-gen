package com.fahmydiab.dqlgen;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.document.Document;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class DqlQueryGeneratorService {
    private final ChatClient chatClient;
    private final EmbeddingModel embeddingModel;
    private final DocumentEmbeddingRepository repository;

    @Value("classpath:/prompts/dql-reference.st")
    private Resource dqlPromptTemplate;


    public DqlQueryGeneratorService(ChatClient.Builder builder, VectorStore vectorStore, EmbeddingModel embeddingModel, DocumentEmbeddingRepository repository) {
        this.chatClient = builder.build();
        this.embeddingModel = embeddingModel;
        this.repository = repository;
    }

    public String generateDqlQuery(String question) {
//        List<Document> retrievedDocs = vectorStore.similaritySearch(SearchRequest.builder().query(question).topK(3).build());
        float[] queryEmbedding = embeddingModel.embed(question);
        List<DocumentEmbedding> retrievedDocs = repository.findSimilarDocuments(queryEmbedding, 5);
        List<String> similarDocs = new ArrayList<>();
        if (retrievedDocs != null) {
            similarDocs = retrievedDocs.stream().map(DocumentEmbedding::getContent).toList();
        }
        PromptTemplate promptTemplate = new PromptTemplate(dqlPromptTemplate);
        Map<String, Object> promptParameters = new HashMap<>();
        promptParameters.put("input", "\nConvert this request into a Dynatrace DQL query:\n" + question);
        promptParameters.put("documents", String.join("\n", similarDocs));
        return chatClient.prompt(promptTemplate.create(promptParameters)).call().content();
    }
}
