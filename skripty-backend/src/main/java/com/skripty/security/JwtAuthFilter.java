package com.skripty.security;

import com.skripty.repository.UserRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User.UserBuilder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;

/**
 * Runs once per request. Reads the "Authorization: Bearer <token>" header,
 * validates the JWT, and — if valid — tells Spring Security who the caller is
 * for the rest of this request. If there's no token or it's invalid, the
 * request simply continues unauthenticated and Spring Security's access
 * rules (see SecurityConfig) decide whether that's allowed.
 */
@Component
@RequiredArgsConstructor
public class JwtAuthFilter extends OncePerRequestFilter {

    private static final String HEADER = "Authorization";
    private static final String PREFIX = "Bearer ";

    private final JwtService jwtService;
    private final UserRepository userRepository;

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain chain
    ) throws ServletException, IOException {

        String authHeader = request.getHeader(HEADER);
        if (authHeader == null || !authHeader.startsWith(PREFIX)) {
            chain.doFilter(request, response);
            return;
        }

        String token = authHeader.substring(PREFIX.length());

        try {
            String email = jwtService.extractEmail(token);
            boolean alreadyAuthenticated = SecurityContextHolder.getContext().getAuthentication() != null;

            if (email != null && !alreadyAuthenticated) {
                boolean userExists = userRepository.existsByEmail(email);
                if (userExists && jwtService.isTokenValid(token, email)) {
                    UserDetails userDetails = buildUserDetails(email);
                    var authToken = new UsernamePasswordAuthenticationToken(
                            userDetails, null, Collections.emptyList());
                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                }
            }
        } catch (Exception ex) {
            // Any JWT parsing error (expired, malformed, bad signature) just means
            // "not authenticated" — we don't leak details, and we let the request
            // continue so SecurityConfig can reject it with a clean 401/403.
            SecurityContextHolder.clearContext();
        }

        chain.doFilter(request, response);
    }

    private UserDetails buildUserDetails(String email) {
        UserBuilder builder = org.springframework.security.core.userdetails.User.builder();
        return builder.username(email).password("").authorities(Collections.emptyList()).build();
    }
}
