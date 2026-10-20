package com.skripty.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.skripty.dto.gemini.GeneratedStudyMaterial;
import com.skripty.entity.StudyDocument;
import com.skripty.repository.StudyDocumentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

/**
 * Runs AI generation in the background on the geminiTaskExecutor pool (see AsyncConfig).
 * Lives in its own bean on purpose: Spring's @Async only works when the method is
 * called from ANOTHER bean. If DocumentService called its own @Async method, it would
 * silently run on the HTTP request thread instead.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class StudyGenerationWorker {

    private final StudyDocumentRepository documentRepository;
    private final GeminiService geminiService;
    private final ObjectMapper objectMapper;

    @Async("geminiTaskExecutor")
    public void generate(Long documentId, String sourceText) {
        documentRepository.findById(documentId).ifPresent(doc -> {
            doc.setStatus(StudyDocument.GenerationStatus.GENERATING);
            documentRepository.save(doc);

            try {
                GeneratedStudyMaterial result = geminiService.generateStudyMaterial(sourceText);

                doc.setSummaryText(result.summary());
                doc.setTestQuestionsJson(objectMapper.writeValueAsString(result.testQuestions()));
                doc.setFlashcardsJson(objectMapper.writeValueAsString(result.flashcards()));
                doc.setStatus(StudyDocument.GenerationStatus.READY);

            } catch (Exception e) {
                log.error("Generation failed for document {}", documentId, e);
                doc.setStatus(StudyDocument.GenerationStatus.FAILED);
            }

            documentRepository.save(doc);
        });
    }
}
