package com.example.HAllTicket.util;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

/**
 * Utility for password hashing and verification using BCrypt.
 * Passwords already hashed (e.g. start with $2a$) are left unchanged when "encoding".
 */
public final class PasswordUtil {

    private static final BCryptPasswordEncoder ENCODER = new BCryptPasswordEncoder();

    private PasswordUtil() {}

    /**
     * Hash a raw password. If the value is already a BCrypt hash (starts with $2a$),
     * it is returned as-is to support migration.
     */
    public static String encode(String rawPassword) {
        if (rawPassword == null || rawPassword.isEmpty()) {
            return rawPassword;
        }
        if (rawPassword.startsWith("$2a$") || rawPassword.startsWith("$2b$")) {
            return rawPassword;
        }
        return ENCODER.encode(rawPassword);
    }

    /**
     * Verify a raw password against a stored hash.
     */
    public static boolean matches(String rawPassword, String encodedPassword) {
        if (rawPassword == null || encodedPassword == null) {
            return false;
        }
        if (encodedPassword.startsWith("$2a$") || encodedPassword.startsWith("$2b$")) {
            return ENCODER.matches(rawPassword, encodedPassword);
        }
        return rawPassword.equals(encodedPassword);
    }
}
