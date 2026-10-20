package com.skripty.service;

import com.skripty.dto.AuthResponse;
import com.skripty.dto.LoginRequest;
import com.skripty.dto.RegisterRequest;
import com.skripty.entity.User;
import com.skripty.exception.ApiException;
import com.skripty.repository.UserRepository;
import com.skripty.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new ApiException(HttpStatus.CONFLICT, "An account with this email already exists.");
        }

        User user = User.builder()
                .email(request.email().toLowerCase().trim())
                .passwordHash(passwordEncoder.encode(request.password()))
                .fullName(request.fullName().trim())
                .build();

        userRepository.save(user);

        String token = jwtService.generateToken(user.getEmail());
        return new AuthResponse(token, user.getEmail(), user.getFullName());
    }

    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.email().toLowerCase().trim())
                .orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, "Invalid email or password."));

        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            // Same error message as "user not found" above — don't reveal which part was wrong.
            throw new ApiException(HttpStatus.UNAUTHORIZED, "Invalid email or password.");
        }

        String token = jwtService.generateToken(user.getEmail());
        return new AuthResponse(token, user.getEmail(), user.getFullName());
    }
}
