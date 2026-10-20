package com.skripty.controller;

import com.skripty.dto.PasteTextRequest;
import com.skripty.dto.StudyDocumentResponse;
import com.skripty.entity.StudyDocument;
import com.skripty.service.DocumentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/documents")
@RequiredArgsConstructor
public class DocumentController {

    private final DocumentService documentService;

    @PostMapping(value = "/upload", consumes = "multipart/form-data")
    public ResponseEntity<StudyDocumentResponse> uploadFile(
            @RequestParam("file") MultipartFile file,
            @RequestParam("title") String title
    ) {
        StudyDocument doc = documentService.uploadFile(file, title);
        return ResponseEntity.ok(StudyDocumentResponse.from(doc));
    }

    @PostMapping("/paste")
    public ResponseEntity<StudyDocumentResponse> uploadPastedText(@Valid @RequestBody PasteTextRequest request) {
        StudyDocument doc = documentService.uploadPastedText(request.title(), request.text());
        return ResponseEntity.ok(StudyDocumentResponse.from(doc));
    }

    /**
     * Frontend polls this every couple of seconds until status is READY or
     * FAILED.
     */
    @GetMapping("/{id}")
    public ResponseEntity<StudyDocumentResponse> getDocument(@PathVariable Long id) {
        StudyDocument doc = documentService.getOwnedDocument(id);
        return ResponseEntity.ok(StudyDocumentResponse.from(doc));
    }

    @GetMapping
    public ResponseEntity<List<StudyDocumentResponse>> listDocuments() {
        List<StudyDocumentResponse> docs = documentService.listOwnedDocuments().stream()
                .map(StudyDocumentResponse::summaryOnly)
                .toList();
        return ResponseEntity.ok(docs);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteDocument(@PathVariable Long id) {
        documentService.deleteOwnedDocument(id);
        return ResponseEntity.noContent().build();
    }
}
