package com.fahmydiab.dqlgen;

import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/dynatrace")
public class DynatraceQueryController {
    private final DqlQueryGeneratorService queryService;

    public DynatraceQueryController(DqlQueryGeneratorService queryService) {
        this.queryService = queryService;
    }

    @GetMapping("/generate-query")
    public String generateQuery(@RequestParam String query) {
        return queryService.generateDqlQuery(query);
    }
}
