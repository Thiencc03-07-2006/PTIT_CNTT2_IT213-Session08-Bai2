package com.rikkei.crm.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.ai.reader.markdown.MarkdownDocumentReader;
import org.springframework.ai.reader.markdown.config.MarkdownDocumentReaderConfig;
import org.springframework.ai.transformer.splitter.TextSplitter;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class DocumentIngestionService {

    private final VectorStore vectorStore;

    @Qualifier("refundProcessSplitter")
    private final TextSplitter refundProcessSplitter;

    @Qualifier("loyaltyPolicySplitter")
    private final TextSplitter loyaltyPolicySplitter;

    @Transactional
    public void ingestDocument(
            Resource resource,
            String category,
            String sourceFile
    ) {

        try {

            // Kiểm tra file
            if (resource == null || !resource.exists()) {
                throw new IllegalArgumentException(
                        "Document not found: " + sourceFile
                );
            }

            log.info(
                    "Starting document ingestion. file={}, category={}",
                    sourceFile,
                    category
            );

            // Đọc Markdown
            MarkdownDocumentReaderConfig config =
                    MarkdownDocumentReaderConfig.builder()
                            .withAdditionalMetadata("category", category)
                            .withAdditionalMetadata("source_file", sourceFile)
                            .build();

            MarkdownDocumentReader reader =
                    new MarkdownDocumentReader(resource, config);

            List<Document> documents = reader.get();

            log.info(
                    "Markdown document loaded successfully. documents={}",
                    documents.size()
            );

            // Chọn chiến lược Chunking
            TextSplitter splitter = switch (category.toLowerCase()) {

                case "loyalty" -> loyaltyPolicySplitter;

                case "refund" -> refundProcessSplitter;

                default -> refundProcessSplitter;
            };

            log.info(
                    "Using splitter: {}",
                    splitter.getClass().getSimpleName()
            );

            // Chunking
            List<Document> chunks = splitter.apply(documents);

            log.info(
                    "Document splitting completed. chunks={}",
                    chunks.size()
            );

            // Bổ sung metadata
            chunks.forEach(document -> {
                document.getMetadata().put("category", category);
                document.getMetadata().put("source_file", sourceFile);
            });

            // Lưu vào pgvector
            vectorStore.add(chunks);

            log.info(
                    "Document ingestion completed successfully. file={}, category={}, chunks={}",
                    sourceFile,
                    category,
                    chunks.size()
            );

        } catch (Exception e) {

            log.error(
                    "Document ingestion failed. file={}, category={}, error={}",
                    sourceFile,
                    category,
                    e.getMessage(),
                    e
            );

            throw new RuntimeException(
                    "Failed to ingest document: " + sourceFile,
                    e
            );
        }
    }
}