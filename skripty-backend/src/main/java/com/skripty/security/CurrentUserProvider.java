package com.skripty.security;

import com.skripty.entity.User;
import com.skripty.exception.ApiException;
import com.skripty.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

/** Resolves the authenticated User entity for the current request from the JWT-derived security context. */
@Component
@RequiredArgsConstructor
public class CurrentUserProvider {

    private final UserRepository userRepository;

    public User getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !(auth.getPrincipal() instanceof UserDetails userDetails)) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, "Not authenticated.");
        }
        return userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, "User no longer exists."));
    }
}
