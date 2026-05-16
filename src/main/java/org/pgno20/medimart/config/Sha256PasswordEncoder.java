package org.pgno20.medimart.config;

import org.springframework.security.crypto.password.PasswordEncoder;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Custom PasswordEncoder that wraps the existing SHA-256 hashing logic.
 *
 * NOTE: SHA-256 without a salt is not ideal for production — see the
 * project analysis report for the BCrypt upgrade recommendation.
 * This encoder keeps the existing hash format so no password re-hashing
 * is required during the Spring Security migration.
 */
public class Sha256PasswordEncoder implements PasswordEncoder {

    @Override
    public String encode(CharSequence rawPassword) {
        return sha256Hex(rawPassword.toString());
    }

    /**
     * Compares a raw (plain-text) password against a stored SHA-256 hash.
     * Spring Security calls this during authentication.
     */
    @Override
    public boolean matches(CharSequence rawPassword, String encodedPassword) {
        return sha256Hex(rawPassword.toString()).equals(encodedPassword);
    }

    private String sha256Hex(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(input.getBytes(StandardCharsets.UTF_8));
            StringBuilder hex = new StringBuilder(2 * hash.length);
            for (byte b : hash) {
                String h = Integer.toHexString(0xff & b);
                if (h.length() == 1) hex.append('0');
                hex.append(h);
            }
            return hex.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 not available", e);
        }
    }
}
