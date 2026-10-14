# Spring AI Lab — Project Overview

This is a hands-on learning project for **Spring Boot 4** and **Spring AI 2.x**.
It is intentionally small: one module per concept, so you can read the whole
codebase in an afternoon.

## Modules

### 1. Web UI (`web` package + `templates/index.html`)
A single Thymeleaf page with four sections: Chat, Prompt Templates, Agent, and RAG.
Every section calls a REST endpoint with `fetch()`. Open your browser's devtools
Network tab while using it to see the API layer in action.

### 2. Chat API (`api/ChatController.java`)
The "hello world" of Spring AI:
- `POST /api/chat` — one request, one response via `ChatClient`.
- `GET /api/chat/stream?message=...` — token-by-token streaming over SSE
  (this is why the project uses `spring-boot-starter-webflux`).
- `POST /api/chat/explain` and `/api/chat/review` — prompt template demos.

### 3. Prompt templates (`prompt/PromptService.java`)
Shows `PromptTemplate` with `{placeholders}`, and the explicit
system-message / user-message split. Prompt engineering 101:
instructions go in the system message, input goes in the user message.

### 4. AI agent (`agent/AssistantTools.java`, `api/AgentController.java`)
Three `@Tool` methods: current date/time, a calculator, and a currency converter.
`POST /api/agent` passes the tools to the model with one line:
`chatClient.prompt()...tools(tools)`. The model decides when to call them —
that decision loop is what makes it an "agent" rather than a chatbot.

### 5. Documentation RAG (`rag/` package)
- `DocumentLoader` (an `ApplicationRunner`) reads every `.md` file in
  `src/main/resources/docs/`, splits them with `TokenTextSplitter`, and stores
  the chunks in an in-memory `SimpleVectorStore`. This runs at startup.
- `RagService` retrieves the most similar chunks for a question and instructs
  the model to answer **only** from that context.
- `POST /api/rag` asks a question; `POST /api/rag/reload` re-ingests the docs
  after you edit them. Try editing this very file, hitting reload, and asking
  about your edit — that's the RAG loop closing.

## Tech choices

- **Ollama** for models: runs locally, no API key, no per-token cost.
  Chat model `llama3.1`, embedding model `nomic-embed-text`.
- **WebFlux** instead of WebMVC: needed for the streaming chat endpoint.
- **SimpleVectorStore**: in-memory vector store, zero infrastructure.
  Perfect for learning; swap in pgvector or Redis for production.
