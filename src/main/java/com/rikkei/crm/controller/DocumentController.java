package com.rikkei.crm.controller;

import com.rikkei.crm.service.DocumentIngestionService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/documents")
@RequiredArgsConstructor
public class DocumentController {

    private final DocumentIngestionService ingestionService;

    @PostMapping("/ingest")
    public ResponseEntity<String> ingest(
            @RequestParam(defaultValue = "cskh")
            String category) {

        Resource resource =
                new ClassPathResource("docs/cskh_quytrinh.md");

        ingestionService.ingestDocument(
                resource,
                category,
                "cskh_quytrinh.md"
        );

        return ResponseEntity.ok("Document ingested successfully.");
    }
}