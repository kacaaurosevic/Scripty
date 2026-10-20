package com.skripty.dto.gemini;

import java.util.List;

public record TestQuestionDto(
        String question,
        List<String> options,
        String correctAnswer,
        String explanation
) {}
