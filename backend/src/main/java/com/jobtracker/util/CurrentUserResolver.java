package com.jobtracker.util;

import com.jobtracker.repository.UserRepository;
import com.jobtracker.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import jakarta.servlet.http.HttpServletRequest;
import java.util.UUID;

/**
 * Resolves the currently authenticated user's UUID from the JWT in the current request.
 * Avoids an extra DB query — userId is embedded in the JWT claims.
 */
@Component
@RequiredArgsConstructor
public class CurrentUserResolver {

    private final JwtUtil jwtUtil;

    public UUID getCurrentUserId() {
        HttpServletRequest request = ((ServletRequestAttributes)
                RequestContextHolder.currentRequestAttributes()).getRequest();
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            String userId = jwtUtil.extractUserId(token);
            return UUID.fromString(userId);
        }
        throw new IllegalStateException("No valid JWT token found in request");
    }
}
