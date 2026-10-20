package com.skripty.dto;

public record AuthResponse(
        String token,
        String email,
        String fullName
) {}
