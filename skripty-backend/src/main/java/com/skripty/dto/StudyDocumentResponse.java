package com.skripty.dto;

import com.skripty.entity.StudyDocument;

import java.time.Instant;

public record StudyDocumentResponse(
        Long id,
        String title,
        StudyDocument.GenerationStatus status,
        String summaryText,
        String testQuestionsJson,
        String flashcardsJson,
        Instant createdAt
) {
    public static StudyDocumentResponse from(StudyDocument doc) {
        return new StudyDocumentResponse(
                doc.getId(),
                doc.getTitle(),
                doc.getStatus(),
                doc.getSummaryText(),
                doc.getTestQuestionsJson(),
                doc.getFlashcardsJson(),
                doc.getCreatedAt()
        );
    }

    /** Lighter version for list views — skips the (potentially large) generated content. */
    public static StudyDocumentResponse summaryOnly(StudyDocument doc) {
        return new StudyDocumentResponse(
                doc.getId(), doc.getTitle(), doc.getStatus(),
                null, null, null, doc.getCreatedAt()
        );
    }
}
