package com.rikkei.crm.config;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.transformer.splitter.TextSplitter;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration
public class TextSplitterConfig {

    /**
     * Token-based Chunking (Loại A)
     */
    @Bean("refundProcessSplitter")
    public TextSplitter refundProcessSplitter() {

        return TokenTextSplitter.builder()
                .withChunkSize(600)
                .withMinChunkSizeChars(120)
                .withMinChunkLengthToEmbed(5)
                .withMaxNumChunks(10000)
                .withKeepSeparator(true)
                .build();
    }

    /**
     * Header-based Chunking (Loại B)
     *
     * MarkdownDocumentReader đã chia tài liệu theo các heading,
     * vì vậy chỉ cần TokenTextSplitter với chunk lớn để giữ nguyên
     * từng section.
     */
    @Bean("loyaltyPolicySplitter")
    public TextSplitter loyaltyPolicySplitter() {

        return TokenTextSplitter.builder()
                .withChunkSize(2000)
                .withMinChunkSizeChars(50)
                .withMinChunkLengthToEmbed(5)
                .withMaxNumChunks(10000)
                .withKeepSeparator(true)
                .build();
    }

    @PostConstruct
    public void logBeans() {
        log.info("TextSplitter beans registered successfully.");
        log.info("- refundProcessSplitter -> TokenTextSplitter");
        log.info("- loyaltyPolicySplitter -> TokenTextSplitter (Header-aware via MarkdownDocumentReader)");
    }
}