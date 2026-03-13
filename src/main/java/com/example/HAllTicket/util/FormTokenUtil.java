package com.example.HAllTicket.util;

import jakarta.servlet.http.HttpSession;
import java.util.UUID;

/**
 * Generates and validates one-time form tokens to prevent duplicate submissions on refresh.
 * Store expected token in session; validate and invalidate on POST.
 */
public final class FormTokenUtil {

    private static final String SESSION_ATTR = "formToken";

    private FormTokenUtil() {}

    /**
     * Generate a new token, store it in the session, and return it for the form.
     */
    public static String generateToken(HttpSession session) {
        String token = UUID.randomUUID().toString();
        session.setAttribute(SESSION_ATTR, token);
        return token;
    }

    /**
     * Validate the submitted token and invalidate it (one-time use).
     * Returns true only if the token is present and matches the session token.
     */
    public static boolean validateAndInvalidate(HttpSession session, String submittedToken) {
        if (submittedToken == null || submittedToken.isEmpty()) {
            return false;
        }
        String expected = (String) session.getAttribute(SESSION_ATTR);
        session.removeAttribute(SESSION_ATTR);
        return expected != null && expected.equals(submittedToken);
    }
}
