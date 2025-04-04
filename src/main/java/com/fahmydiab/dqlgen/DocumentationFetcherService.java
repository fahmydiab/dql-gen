package com.fahmydiab.dqlgen;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.stereotype.Service;
import java.io.IOException;

@Service
public class DocumentationFetcherService {
    public String fetchDocumentation(String url) {
        try {
            Document doc = Jsoup.connect(url).get();
            return doc.text();
        } catch (IOException e) {
            return "Error fetching documentation: " + e.getMessage();
        }
    }
}
