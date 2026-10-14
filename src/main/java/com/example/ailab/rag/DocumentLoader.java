package com.example.ailab.rag;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.document.Document;
import org.springframework.ai.reader.TextReader;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * The <strong>E</strong> and <strong>T</strong> of the RAG ETL pipeline, run once at startup:
 *
 * <ol>
 *   <li><strong>Extract</strong>: read every {@code .md} file under {@code classpath:docs/}
 *       with {@link TextReader}.</li>
 *   <li><strong>Transform</strong>: split long documents into chunks with
 *       {@link TokenTextSplitter} (LLMs have context limits; chunks keep
 *       retrieval precise).</li>
 *   <li><strong>Load</strong>: embed each chunk and store it in the
 *       {@link VectorStore} (the "L" happens inside {@code vectorStore.add}).</li>
 * </ol>
 */
@Component
public class DocumentLoader implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(DocumentLoader.class);

    private final ResourcePatternResolver resourceResolver;
    private final VectorStore vectorStore;

    public DocumentLoader(ResourcePatternResolver resourceResolver, VectorStore vectorStore) {
        this.resourceResolver = resourceResolver;
        this.vectorStore = vectorStore;
    }

    @Override
    public void run(ApplicationArguments args) {
        try {
            loadDocuments();
        } catch (Exception e) {
            // Fail soft: the web UI and chat still work once Ollama is up,
            // and POST /api/rag/reload retries the ingestion.
            log.warn("""
                    RAG documents NOT loaded — is Ollama running?
                    Start it with `ollama serve`, pull the models
                    (`ollama pull llama3.1`, `ollama pull nomic-embed-text`),
                    then POST /api/rag/reload. Cause: {}""",
                    e.getMessage());
        }
    }

    /**
     * (Re-)loads the docs. Also exposed via {@code POST /api/rag/reload}.
     */
    public int loadDocuments() {
        try {
            Resource[] resources = resourceResolver.getResources("classpath:docs/*.md");
            // Explicit chunking settings (these were the classic defaults):
            // ~800 tokens per chunk, keep chunks >= 350 chars, keep separators.
            var splitter = TokenTextSplitter.builder()
                    .withChunkSize(800)
                    .withMinChunkSizeChars(350)
                    .withMinChunkLengthToEmbed(5)
                    .withMaxNumChunks(10000)
                    .withKeepSeparator(true)
                    .build();
            List<Document> chunks = new ArrayList<>();

            for (Resource resource : resources) {
                List<Document> documents = new TextReader(resource).read();
                // Tag each chunk with its source file so answers can cite it
                for (Document document : documents) {
                    document.getMetadata().put("source", resource.getFilename());
                }
                chunks.addAll(splitter.apply(documents));
            }

            vectorStore.add(chunks);
            log.info("RAG ready: loaded {} chunks from {} documents", chunks.size(), resources.length);
            return chunks.size();
        } catch (Exception e) {
            throw new IllegalStateException("Failed to load RAG documents", e);
        }
    }
}
