# Spring AI Lab

A small, runnable project to learn **Spring Boot 4** + **Spring AI 2.x** by doing.
One module per concept — read the whole codebase in an afternoon:

| Module | What you learn | Key file |
|---|---|---|
| Web UI | Thymeleaf + fetch() calling your own API | `templates/index.html` |
| Chat API | `ChatClient`, streaming with SSE | `api/ChatController.java` |
| Prompt templates | `PromptTemplate`, system vs user messages | `prompt/PromptService.java` |
| AI agent | `@Tool` methods + tool calling | `agent/AssistantTools.java` |
| Documentation RAG | ETL pipeline, `VectorStore`, retrieval | `rag/` package |

## Prerequisites

1. **JDK 17+** — `java -version`
2. **Maven** — or use `./mvnw` if you add the wrapper
3. **Ollama** — [ollama.com/download](https://ollama.com/download), then:
   ```
   ollama pull llama3.1
   ollama pull nomic-embed-text
   ```
   No API keys, no per-token cost — everything runs locally.

## Run it

```bash
cd spring-ai-lab
mvn spring-boot:run
```

Open http://localhost:8080 — four sections, one per concept.
**Start Ollama first** (`ollama serve` if it's not running as a service),
otherwise the chat calls will fail.

## Suggested learning path

Do these in order. Each one builds on the last:

1. **Chat** — Open `api/ChatController.java`. See how `ChatClient.prompt().user(...).call().content()`
   is the whole API. Try the `/stream` endpoint and watch tokens arrive.
2. **Prompts** — Open `prompt/PromptService.java`. Change the system message in
   `roastMyCode`, restart, and feel how much the system prompt steers behavior.
   Add your own template with a `{placeholder}`.
3. **Agent** — Open `agent/AssistantTools.java`. Ask the agent
   *"What time is it in Tokyo, and what is (15/100)*240?"* and watch the server
   log: the model calls `currentDateTime` and `calculate`, then answers.
   **Exercise:** add a `@Tool` that reads a file, or that calls a public REST API
   (weather, for example). Notice the rule: tools are deterministic Java —
   no LLM inside a tool.
4. **RAG** — Open `rag/DocumentLoader.java` (the ETL pipeline: Extract →
   Transform → Load, runs at startup) and `rag/RagService.java` (retrieve →
   stuff into prompt → generate). Edit a file in `src/main/resources/docs/`,
   hit `POST /api/rag/reload`, and ask about your edit. That's the RAG loop.
   **Exercise:** point the loader at your own docs folder.

## API cheat sheet

```
POST /api/chat                 {"message": "..."}
GET  /api/chat/stream?message=...        (SSE)
POST /api/chat/explain         {"topic": "...", "level": "..."}
POST /api/chat/review          {"code": "..."}
POST /api/agent                {"message": "..."}
POST /api/rag                  {"question": "..."}
POST /api/rag/reload           {}
```

## Swap Ollama for OpenAI (optional)

1. Replace the `spring-ai-starter-model-ollama` dependency with
   `spring-ai-starter-model-openai`.
2. Set in `application.properties`:
   ```
   spring.ai.openai.api-key=${OPENAI_API_KEY}
   spring.ai.openai.chat.model=gpt-4o-mini
   spring.ai.openai.embedding.model=text-embedding-3-small
   ```
   Nothing else changes — that's the point of Spring AI's portable abstractions.

## Week 1 — Spring core

Code lives in `week1/day1` through `week1/day5`, plus `labautoconfigure` (Day 3)
and `actuator/NotificationHealthIndicator` (Day 5). Try it:

```bash
mvn test
mvn spring-boot:run
curl localhost:8080/api/week1/welcome
curl localhost:8080/api/week1/notify
curl localhost:8080/api/week1/greeting
curl localhost:8080/api/week1/transactions
curl localhost:8080/actuator/health
curl localhost:8080/actuator/info

# same code, prod notifier
mvn spring-boot:run -Dspring-boot.run.arguments=--spring.profiles.active=prod

# Day 1: read BeanCurrentlyInCreationException, then stop
mvn spring-boot:run -Dspring-boot.run.arguments=--spring.profiles.active=dev,circular

# Day 2: startup must fail — blank notify.api-key
mvn spring-boot:run -Dspring-boot.run.arguments=--spring.profiles.active=broken
```

Open `DataSourceAutoConfiguration` from `spring-boot-starter-jdbc` and compare it
with `com.example.labautoconfigure.GreetingAutoConfiguration`.

### Architecture decisions

- Constructor injection only. Field injection hides required dependencies and needs reflection to test.
- One `Notifier` per profile (`dev` logs, `prod` pretends to email). Behavior changes with the profile, not a code edit.
- `notify.*` is a validated `@ConfigurationProperties` record. A blank API key fails startup.
- `GreetingAutoConfiguration` lives outside component scan and backs off when the app defines its own `GreetingService`.
- `@Transactional` is a proxy. `this.withdraw()` ignores `REQUIRES_NEW`; the injected proxy does not.
- Actuator exposes `health`, `info`, and `metrics`. `env` stays off because it would leak `notify.api-key`. Dev shows health details; prod does not.
- AI stays on Ollama through `spring-ai-starter-model-ollama`. Week 1 does not change that wiring.
- Blocking AI and JDBC work stays off Netty's event loop (`boundedElastic`).
- H2 exists so Day 4 has a real transaction manager from `DataSourceAutoConfiguration`.
- The circular pair is `@Profile("circular")`, so `ApplicationTests.contextLoads()` stays green.

## Where to go next

- **Advisors** (`QuestionAnswerAdvisor`, `SafeGuardAdvisor`): the idiomatic,
  one-line way to do what `RagService` does by hand here.
- **MCP** (Model Context Protocol): expose your tools to *other* AI clients,
  or consume external tool servers — Spring AI has an MCP client/server.
- **Persistent vector stores**: swap `SimpleVectorStore` for pgvector/Redis
  when the doc set outgrows memory.
- **Observability**: Spring AI 2.x ships Micrometer tracing for chat calls —
  add `spring-boot-starter-actuator` and watch.

## Troubleshooting

- *"Connection refused" on chat calls* → Ollama isn't running. Start it first.
- *"block()/blockFirst()/blockLast() are blocking, which is not supported in thread
  reactor-http-nio-..."* → you called a blocking API on a Netty event-loop thread.
  This is WebFlux rule #1: never block the event loop. The lab's controllers wrap
  every blocking Spring AI call in `Mono.fromCallable(...).subscribeOn(Schedulers.boundedElastic())`.
  The `/stream` endpoint doesn't need it — it's reactive end to end, which is exactly
  why streaming worked while the blocking endpoints failed.
- *Slow first RAG load* → the embedding model downloads on first use; subsequent
  starts are fast.
- *Streaming endpoint hangs* → needs WebFlux (already configured); don't swap
  `spring-boot-starter-webflux` for `webmvc` without changing the controller.
