package com.skripty.dto.gemini;

import java.util.List;

public record GeneratedStudyMaterial(
        String summary,
        List<TestQuestionDto> testQuestions,
        List<FlashcardDto> flashcards
) {}
