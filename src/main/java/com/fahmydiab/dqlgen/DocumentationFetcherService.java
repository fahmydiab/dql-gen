package com.fahmydiab.dqlgen;

import org.json.JSONArray;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

@Component
public class DocumentationFetcherService {

    private static final Logger logger = Logger.getLogger(DocumentationFetcherService.class.getName());
    private final List<JSONObject> scrapedData = new LinkedList<>();
    private final Set<String> visitedUrls = new HashSet<>();
    private final Queue<String> queue = new LinkedList<>();

    @Value("${crawler.depth.limit:3}")
    private int depthLimit;

    private String allowedDomain;

    public List<String> fetchDocumentation(String startUrl) {
        try {
            URI uri = new URI(startUrl);
            this.allowedDomain = uri.getHost();
            queue.offer(startUrl);
            visitedUrls.add(startUrl);
            crawl();
            writeToJson("output_v2.json");
            List<String> documents = loadDocuments();
            return splitDocuments(documents);
        } catch (URISyntaxException e) {
            logger.log(Level.SEVERE, "Invalid start URL: " + startUrl, e);
            return new ArrayList<>();
        }
    }

    private void crawl() {
        int currentDepth = 0;
        while (!queue.isEmpty() && currentDepth <= depthLimit) {
            int levelSize = queue.size(); // Process all URLs at the current depth
            for (int i = 0; i < levelSize; i++) {
                String currentUrl = queue.poll();
                if (currentUrl != null) {
                    crawlPage(currentUrl);
                }
            }
            currentDepth++;
        }
    }

    private void crawlPage(String url) {
        logger.log(Level.INFO, "Crawling URL: {0}", url);
        try {
            if (!isWithinAllowedDomain(url)) {
                logger.log(Level.WARNING, "Skipping URL outside allowed domain: {0}", url);
                return;
            }

            Document doc = Jsoup.connect(url)
                    .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/58.0.3029.110 Safari/537.3")
                    .get();

            JSONObject data = new JSONObject();
            data.put("url", url);
            data.put("title", doc.title());
            Elements paragraphs = doc.select("p");
            String content = paragraphs.stream()
                    .map(Element::text)
                    .collect(java.util.stream.Collectors.joining(" "));
            data.put("content", content.trim());
            scrapedData.add(data);

            Elements links = doc.select("a[href]");
            for (Element link : links) {
                String href = link.attr("href");
                try {
                    String absoluteUrl = URI.create(url).resolve(href).toString();
                    if (absoluteUrl.startsWith("http") && !visitedUrls.contains(absoluteUrl) && isWithinAllowedDomain(absoluteUrl)) {
                        visitedUrls.add(absoluteUrl);
                        queue.offer(absoluteUrl);
                        logger.log(Level.FINE, "Discovered new URL: {0}", absoluteUrl);
                    } else if (!isWithinAllowedDomain(absoluteUrl) && absoluteUrl.startsWith("http")) {
                        logger.log(Level.FINE, "Skipping external URL: {0}", absoluteUrl);
                    }
                } catch (IllegalArgumentException e) {
                    logger.log(Level.WARNING, "Error resolving URL: {0} from {1} - {2}", new Object[]{href, url, e.getMessage()});
                }
            }

        } catch (IOException e) {
            logger.log(Level.WARNING, "Error crawling URL: {0} - {1}", new Object[]{url, e.getMessage()});
        }
    }
    private boolean isWithinAllowedDomain(String url) {
        if (allowedDomain == null || url == null) {
            return false;
        }
        try {
            URI uri = new URI(url);
            String host = uri.getHost();
            return host != null && host.equalsIgnoreCase(allowedDomain);
        } catch (URISyntaxException e) {
            logger.log(Level.WARNING, "Error parsing URL for domain check: {0} - {1}", new Object[]{url, e.getMessage()});
            return false;
        }
    }

    private List<String> splitDocuments(List<String> documents) {
        List<String> splits = new ArrayList<>();
        for (String doc : documents) {
            int chunkSize = 300;
            int chunkOverlap = 100;
            if (doc.length() <= chunkSize) {
                splits.add(doc);
            } else {
                for (int i = 0; i <= doc.length() - chunkSize; i += (chunkSize - chunkOverlap)) {
                    int end = Math.min(i + chunkSize, doc.length());
                    splits.add(doc.substring(i, end));
                }
            }
        }
        return splits;
    }

    private List<String> loadDocuments() {
        List<String> contents = new ArrayList<>();
        try (FileReader reader = new FileReader("output_v2.json")) {
            StringBuilder sb = new StringBuilder();
            int i;
            while ((i = reader.read()) != -1) {
                sb.append((char) i);
            }
            JSONArray jsonArray = new JSONArray(sb.toString());
            for (int j = 0; j < jsonArray.length(); j++) {
                JSONObject jsonObject = jsonArray.getJSONObject(j);
                if (jsonObject.has("content")) {
                    contents.add(jsonObject.getString("content"));
                }
            }
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Error loading documents from output.json", e);
        }
        return contents;
    }

    private void writeToJson(String filename) {
        JSONArray jsonArray = new JSONArray(scrapedData);
        try (FileWriter file = new FileWriter(filename)) {
            file.write(jsonArray.toString(2)); // Use toString(2) for pretty printing
            logger.log(Level.INFO, "Scraped data written to {0}", filename);
        } catch (IOException e) {
            logger.log(Level.WARNING, "Error writing to JSON file: {0} - {1}", new Object[]{filename, e.getMessage()});
        }
    }
}
