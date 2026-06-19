package com.backend.Skytouch.authentication.security;

import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import static org.assertj.core.api.Assertions.assertThat;

class PasswordVerifierTest {

    private final PasswordVerifier passwordVerifier = new PasswordVerifier();
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @Test
    void matches_legacyPlaintextPassword() {
        assertThat(passwordVerifier.matches("secret", "secret", passwordEncoder)).isTrue();
    }

    @Test
    void matches_bcryptPassword() {
        String hash = passwordEncoder.encode("secret");
        assertThat(passwordVerifier.matches("secret", hash, passwordEncoder)).isTrue();
    }

    @Test
    void isLegacyPassword_detectsPlaintext() {
        assertThat(passwordVerifier.isLegacyPassword("plain")).isTrue();
        assertThat(passwordVerifier.isLegacyPassword(passwordEncoder.encode("plain"))).isFalse();
    }
}
