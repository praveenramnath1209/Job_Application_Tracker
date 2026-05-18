package com.jobtracker.util;

import com.jobtracker.security.JwtUtil;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.UUID;

/**
 * Utility for extracting the authenticated user's ID from the SecurityContext.
 */
public final class SecurityUtils {

    private SecurityUtils() {}

    public static UUID getCurrentUserId(JwtUtil jwtUtil) {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (principal instanceof UserDetails userDetails) {
            // Extract from credentials — we stored userId in JWT claims
            // The principal here is UserDetails with username=email
            // We need the JWT token to get userId — use request attribute instead
        }
        throw new IllegalStateException("Cannot extract user ID from SecurityContext");
    }
}
