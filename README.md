# Java + LLM Enterprise Demo

A production-ready Spring Boot application that implements every layer
from the **Java + LLM Enterprise Architecture** diagram.

---

## Architecture Mapping

| Diagram Layer | This Project |
|---|---|
| **API Layer** – REST Controllers / Endpoints | `ChatController` |
| **Service Layer** – Business Logic, Workflow, Prompt Management | `ChatService`, `PromptManager` |
| **AI Orchestration Layer** – LangChain4j / Spring AI | `AiOrchestrationService` |
| **Data Access Layer** – JPA / Hibernate | `ChatMessageRepository`, `KnowledgeDocumentRepository` |
| **Vector DB / Knowledge Base** (RAG ②) | `RagService` + pgvector (prod) / H2 (demo) |
| **LLM Provider** (③ Prompt+Context / ④ Response) | Spring AI `ChatClient` → OpenAI or Azure OpenAI |
| **Deployment & Infra** | `Dockerfile`, `docker-compose.yml`, Prometheus, Grafana |
| **Security / Validation / Logging / Monitoring** | `AppConfig`, `GlobalExceptionHandler`, Actuator |

---

## Quick Start

### 1. Run locally (H2 in-memory, no Docker needed)

```bash
export OPENAI_API_KEY=sk-your-key-here
./mvnw spring-boot:run
```

### 2. Run with Docker Compose (PostgreSQL + pgvector + Grafana)

```bash
export OPENAI_API_KEY=sk-your-key-here
docker compose up --build
```

Services:
- App → http://localhost:8080
- H2 Console → http://localhost:8080/h2-console
- Prometheus → http://localhost:9090
- Grafana → http://localhost:3000 (admin / admin)

---

## API Reference

### Single-turn Q&A (no memory)

```bash
curl -X POST http://localhost:8080/api/ask \
  -H "Content-Type: application/json" \
  -d '{"question": "What is Spring AI?"}'
```

**Response:**
```json
{
  "question": "What is Spring AI?",
  "answer": "Spring AI is an abstraction layer for AI/LLM providers..."
}
```

---

### Multi-turn Chat (with session memory)

```bash
# Turn 1
curl -X POST http://localhost:8080/api/chat/session-abc \
  -H "Content-Type: application/json" \
  -d '{"message": "What is RAG?"}'

# Turn 2 — model remembers the conversation
curl -X POST http://localhost:8080/api/chat/session-abc \
  -H "Content-Type: application/json" \
  -d '{"message": "Can you give me a Java example of that?"}'

# Clear session memory
curl -X DELETE http://localhost:8080/api/chat/session-abc
```

---

### Ingest a document into the knowledge base (RAG)

```bash
curl -X POST http://localhost:8080/api/documents \
  -H "Content-Type: application/json" \
  -d '{
    "title": "Company Holiday Policy",
    "content": "Employees receive 20 days of paid leave per year...",
    "source": "pdf"
  }'
```

After ingestion, any question about "holiday" or "leave" will automatically
retrieve this document and include it as context in the LLM prompt.

---

## Switching LLM Providers

### OpenAI (default)
```properties
spring.ai.openai.api-key=${OPENAI_API_KEY}
spring.ai.openai.chat.options.model=gpt-4o
```

### Azure OpenAI
Replace the `spring-ai-openai-spring-boot-starter` dependency in `pom.xml`
with `spring-ai-azure-openai-spring-boot-starter`, then:
```properties
spring.ai.azure.openai.api-key=${AZURE_OPENAI_KEY}
spring.ai.azure.openai.endpoint=https://YOUR_RESOURCE.openai.azure.com
spring.ai.azure.openai.chat.options.deployment-name=gpt-4o
```
Zero application code changes needed — Spring AI's abstraction handles the rest.

---

## Production Upgrade Path

| Feature | Demo | Production |
|---|---|---|
| Database | H2 in-memory | PostgreSQL |
| Vector search | Keyword (LIKE) | pgvector `<=>` cosine similarity |
| Security | Open (demo) | JWT / OAuth2 Resource Server |
| Secrets | `application.properties` | Azure Key Vault / AWS Secrets Manager |
| Deployment | `java -jar` | Kubernetes (see `docker-compose.yml`) |
| Embeddings | Not wired | `spring-ai-pgvector-store` + `EmbeddingClient` |
