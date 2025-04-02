#### docker run --name pgvector-db -e POSTGRES_USER=postgres -e POSTGRES_PASSWORD=secret -p 5432:5432 ankane/pgvector
---- 
# RAG System for Dynatrace Logs Using Spring AI

This project implements a **Retrieval-Augmented Generation (RAG) system** using **Spring AI, pgvector, and a locally deployed AI model**. It enables users to **query Dynatrace logs using natural language**, and the system generates **Dynatrace Query Language (DQL) queries**.

## Features

- Dynamically fetch and embed **Dynatrace DQL documentation**.
- Store documentation in **pgvector** for similarity searches.
- Use **Spring AI** with a local model to generate **DQL queries**.
- REST API for query generation.

---

## **Implementation Steps**

### **1️⃣ Install and Run PostgreSQL with pgvector**

#### **Option 1: Run pgvector with Docker**

```sh
docker run --name pgvector-db -e POSTGRES_USER=postgres -e POSTGRES_PASSWORD=secret -p 5432:5432 ankane/pgvector
```

#### **Option 2: Install pgvector on an Existing PostgreSQL**

```sql
CREATE EXTENSION IF NOT EXISTS vector;
```

---

### **2️⃣ Configure Spring Boot for PostgreSQL & pgvector**

#### **Add Dependencies**

```xml
<dependency>
    <groupId>org.postgresql</groupId>
    <artifactId>postgresql</artifactId>
    <version>42.5.1</version>
</dependency>
<dependency>
    <groupId>io.github.pgvector</groupId>
    <artifactId>pgvector</artifactId>
    <version>0.2.4</version>
</dependency>
<dependency>
    <groupId>org.springframework.ai</groupId>
    <artifactId>spring-ai-ollama-spring-boot-starter</artifactId>
    <version>1.0.0</version>
</dependency>
```

#### **Configure **``

```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/postgres
spring.datasource.username=postgres
spring.datasource.password=secret
spring.datasource.driver-class-name=org.postgresql.Driver
```

---

### **3️⃣ Create a Table for Storing Embedded Documentation**

```sql
CREATE TABLE document_embeddings (
    id SERIAL PRIMARY KEY,
    content TEXT,
    embedding vector(1536) -- Adjust size based on model
);
```

---

### **4️⃣ Fetch Dynatrace Documentation & Store Embeddings**

#### **Java Service to Fetch Documentation**

```java
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
```

#### **Embed Documentation into pgvector**

```java
import org.springframework.ai.embedding.EmbeddingClient;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Service;

@Service
public class DocumentationEmbeddingService {
    private final EmbeddingClient embeddingClient;
    private final VectorStore vectorStore;
    private final DocumentationFetcherService fetcherService;

    public void embedDocumentation(String url) {
        String content = fetcherService.fetchDocumentation(url);
        vectorStore.add(content, embeddingClient.embed(content));
    }
}
```

---

### **5️⃣ Retrieve and Generate a DQL Query**

#### **Generate Queries Using Spring AI**

```java
import org.springframework.ai.chat.ChatClient;
import org.springframework.stereotype.Service;

@Service
public class DqlQueryGeneratorService {
    private final ChatClient chatClient;
    private final VectorStore vectorStore;

    public String generateDqlQuery(String question) {
        String retrievedDocs = vectorStore.similaritySearch(question, 2).toString();
        String prompt = retrievedDocs + "\nConvert this request into a Dynatrace DQL query:\n" + question;
        return chatClient.call(prompt);
    }
}
```

---

### **6️⃣ Create a REST API for Query Generation**

```java
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
```

---

## **7️⃣ Run the System**

### **Step 1: Embed Documentation**

```sh
curl "http://localhost:8080/dynatrace/embed?url=https://docs.dynatrace.com/data-explorer-dql"
```

### **Step 2: Generate a DQL Query**

```sh
curl "http://localhost:8080/dynatrace/generate-query?query=Show error logs from last 24 hours"
```

### **Example Output:**

```sql
fetch logs
| filter loglevel == "ERROR"
| timeframe between now() - 1d and now()
```

---

## **Next Steps**

- 🛠 **Optimize embeddings**: Use chunking for better retrieval.
- 📅 **Schedule updates**: Keep the documentation fresh.
- 🤖 **Improve AI response**: Fine-tune prompts for better query generation.

🚀 **Enjoy querying Dynatrace logs with AI-powered DQL generation!**


