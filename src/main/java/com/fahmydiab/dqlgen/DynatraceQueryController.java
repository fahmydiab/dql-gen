package com.fahmydiab.dqlgen;

import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/dynatrace")
public class DynatraceQueryController {
    private final DqlQueryGeneratorService queryService;
    private final DocumentationEmbeddingService embeddingService;

    public DynatraceQueryController(DqlQueryGeneratorService queryService, DocumentationEmbeddingService embeddingService) {
        this.queryService = queryService;
        this.embeddingService = embeddingService;
    }

    @GetMapping("/generate-query")
    public String generateQuery(@RequestParam String query) {
        return queryService.generateDqlQuery(query);
    }

    @GetMapping("/embed")
    public void embedQuery(@RequestParam String url) {
        embeddingService.embedDocumentation(url);
    }
}
