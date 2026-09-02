package com.backend.Skytouch.authentication.security;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class PasswordVerifier {

    public boolean matches(String rawPassword, String storedPassword, PasswordEncoder passwordEncoder) {
        if (isBcryptHash(storedPassword)) {
            return passwordEncoder.matches(rawPassword, storedPassword);
        }
        return rawPassword.equals(storedPassword);
    }

    public boolean isLegacyPassword(String storedPassword) {
        return !isBcryptHash(storedPassword);
    }

    private boolean isBcryptHash(String storedPassword) {
        return storedPassword.startsWith("$2a$")
                || storedPassword.startsWith("$2b$")
                || storedPassword.startsWith("$2y$");
    }
}
