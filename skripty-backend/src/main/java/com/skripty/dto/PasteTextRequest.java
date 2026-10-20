package com.skripty.dto;

import jakarta.validation.constraints.NotBlank;

public record PasteTextRequest(
        @NotBlank String title,
        @NotBlank String text
) {}
