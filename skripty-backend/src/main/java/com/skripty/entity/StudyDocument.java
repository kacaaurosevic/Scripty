package com.skripty.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

/**
 * One uploaded piece of study material plus everything Gemini generated from it.
 * Named StudyDocument (not Document) to avoid clashing with java.io / org.w3c classes.
 *
 * The generated content (summary, test questions, flashcards) is stored as JSON text
 * columns. This keeps the schema simple for an MVP; if the app grows, these can be
 * split into their own tables (Flashcard, TestQuestion) with a foreign key back here.
 */
@Entity
@Table(name = "study_documents")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StudyDocument {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User owner;

    @Column(nullable = false)
    private String title;

    @Lob
    @Column(nullable = false, columnDefinition = "LONGTEXT")
    private String sourceText;      // raw extracted text from the upload or pasted text

    @Lob
    @Column(columnDefinition = "LONGTEXT")
    private String summaryText;     // AI-generated condensed study notes

    @Lob
    @Column(columnDefinition = "LONGTEXT")
    private String testQuestionsJson;   // JSON array of {question, options[], correctAnswer, explanation}

    @Lob
    @Column(columnDefinition = "LONGTEXT")
    private String flashcardsJson;      // JSON array of {term, definition}

    @Enumerated(EnumType.STRING)
    @Builder.Default
    @Column(nullable = false)
    private GenerationStatus status = GenerationStatus.PENDING;

    @Builder.Default
    @Column(nullable = false)
    private Instant createdAt = Instant.now();

    public enum GenerationStatus {
        PENDING, GENERATING, READY, FAILED
    }
}
