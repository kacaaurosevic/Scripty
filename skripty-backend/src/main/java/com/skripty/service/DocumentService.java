package com.skripty.service;

import com.skripty.entity.StudyDocument;
import com.skripty.entity.User;
import com.skripty.exception.ApiException;
import com.skripty.repository.StudyDocumentRepository;
import com.skripty.security.CurrentUserProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class DocumentService {

    private final StudyDocumentRepository documentRepository;
    private final FileParsingService fileParsingService;
    private final GeminiService geminiService;
    private final StudyGenerationWorker generationWorker;
    private final CurrentUserProvider currentUserProvider;

    public StudyDocument uploadFile(MultipartFile file, String title) {
        String text = fileParsingService.extractText(file);
        return createAndGenerate(title, text);
    }

    public StudyDocument uploadPastedText(String title, String text) {
        String cleaned = fileParsingService.cleanPastedText(text);
        return createAndGenerate(title, cleaned);
    }

    public StudyDocument getOwnedDocument(Long id) {
        User user = currentUserProvider.getCurrentUser();
        return documentRepository.findByIdAndOwner(id, user)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Document not found."));
    }

    public List<StudyDocument> listOwnedDocuments() {
        User user = currentUserProvider.getCurrentUser();
        return documentRepository.findByOwnerOrderByCreatedAtDesc(user);
    }

    private StudyDocument createAndGenerate(String title, String sourceText) {
        User user = currentUserProvider.getCurrentUser();

        StudyDocument doc = StudyDocument.builder()
                .owner(user)
                .title(title)
                .sourceText(sourceText)
                .status(StudyDocument.GenerationStatus.PENDING)
                .build();

        doc = documentRepository.save(doc);

        generationWorker.generate(doc.getId(), sourceText);

        return doc; // returned immediately with status PENDING; frontend polls GET /api/documents/{id}
    }

    public void deleteOwnedDocument(Long id) {
        StudyDocument doc = getOwnedDocument(id); // throws 404 if it's not yours
        documentRepository.delete(doc);
    }
}
