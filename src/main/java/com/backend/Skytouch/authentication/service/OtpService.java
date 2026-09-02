package com.backend.Skytouch.authentication.service;

import com.backend.Skytouch.authentication.config.AuthProperties;
import com.backend.Skytouch.authentication.entity.OtpCode;
import com.backend.Skytouch.authentication.repository.OtpCodeRepository;
import com.backend.Skytouch.common.enums.OtpPurpose;
import com.backend.Skytouch.common.exception.UnauthorizedException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class OtpService {

    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    private final OtpCodeRepository otpCodeRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthProperties authProperties;
    private final EmailService emailService;

    // ─── Email Verification ───────────────────────────────────────────────────

    @Transactional
    public void sendEmailVerificationOtp(UUID userId, String email) {
        sendOtp(userId, email, OtpPurpose.EMAIL_VERIFICATION);
    }

    @Transactional
    public void verifyEmailVerificationOtp(UUID userId, String rawOtp) {
        verifyOtp(userId, rawOtp, OtpPurpose.EMAIL_VERIFICATION);
    }

    // ─── Core send/verify ─────────────────────────────────────────────────────

    @Transactional
    public void sendOtp(UUID userId, String email, OtpPurpose purpose) {
        LocalDateTime now = LocalDateTime.ofInstant(Instant.now(), ZoneOffset.UTC);
        otpCodeRepository.invalidatePending(userId, purpose, now);

        String otp = generateOtp();
        LocalDateTime expiresAt = now.plus(Duration.ofMillis(authProperties.getOtp().getExpirationMs()));
        OtpCode otpCode = OtpCode.builder()
                .userId(userId)
                .codeHash(passwordEncoder.encode(otp))
                .purpose(purpose)
                .expiresAt(expiresAt)
                .build();

        otpCodeRepository.save(otpCode);

        if (authProperties.isLogOtp()) {
            log.info("[OTP] purpose={} email={} code={}", purpose, email, otp);
        }

        log.info("[OTP DEBUG] Created OTP for userId={} purpose={} now={} expiresAt={} expirationMs={}",
                userId, purpose, now, expiresAt, authProperties.getOtp().getExpirationMs());

        // ✅ LOGIN removed — only EMAIL_VERIFICATION and PASSWORD_RESET
        if (purpose == OtpPurpose.EMAIL_VERIFICATION) {
            emailService.sendEmailVerificationOtp(email, otp);
        } else if (purpose == OtpPurpose.PASSWORD_RESET) {
            emailService.sendPasswordResetOtp(email, otp);
        }
    }

    @Transactional
    public void verifyOtp(UUID userId, String rawOtp, OtpPurpose purpose) {
        LocalDateTime now = LocalDateTime.ofInstant(Instant.now(), ZoneOffset.UTC);
        log.info("[OTP DEBUG] Verifying OTP for userId={} purpose={} now={}", userId, purpose, now);

        OtpCode otpCode = otpCodeRepository
                .findByUserIdAndPurposeAndConsumedAtIsNullAndExpiresAtAfter(userId, purpose, now)
                .orElseThrow(() -> {
                    log.error("[OTP DEBUG] OTP not found or expired for userId={} purpose={} now={}", userId, purpose, now);
                    return new UnauthorizedException("Invalid or expired OTP");
                });

        log.info("[OTP DEBUG] Found OTP: expiresAt={} attempts={}", otpCode.getExpiresAt(), otpCode.getAttempts());

        if (otpCode.getAttempts() >= authProperties.getOtp().getMaxAttempts()) {
            otpCode.setConsumedAt(now);
            otpCodeRepository.save(otpCode);
            throw new UnauthorizedException("OTP attempts exceeded. Request a new code.");
        }

        if (!passwordEncoder.matches(rawOtp, otpCode.getCodeHash())) {
            otpCode.setAttempts(otpCode.getAttempts() + 1);
            otpCodeRepository.save(otpCode);
            throw new UnauthorizedException("Invalid or expired OTP");
        }

        otpCode.setConsumedAt(now);
        otpCodeRepository.save(otpCode);
        log.info("[OTP DEBUG] OTP verified successfully for userId={}", userId);
    }

    // ─── Private helpers ──────────────────────────────────────────────────────

    private String generateOtp() {
        int length = authProperties.getOtp().getLength();
        int bound = (int) Math.pow(10, length);
        int floor = bound / 10;
        int code = floor + SECURE_RANDOM.nextInt(bound - floor);
        return String.valueOf(code);
    }
}