package com.backend.Skytouch.authentication.controller;

import com.backend.Skytouch.authentication.apimodel.*;
import com.backend.Skytouch.authentication.service.AuthenticationService;
import com.backend.Skytouch.authentication.service.EmailVerificationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthenticationController {

    private final AuthenticationService authenticationService;
    private final EmailVerificationService emailVerificationService;

    @PostMapping("/register")
    public ResponseEntity<RegisterJobSeekerResponse> register(
            @Valid @RequestBody RegisterJobSeekerRequest request) {
        log.info("Register attempt for email: {}", request.getEmail());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(authenticationService.register(request));
    }

    @PostMapping("/verify-email")
    public ResponseEntity<EmailVerifiedResponse> verifyEmail(
            @Valid @RequestBody VerifyOtpRequest request) {
        log.info("Email verification for email: {}", request.getEmail());
        return ResponseEntity.ok(emailVerificationService.verifyEmail(request));
    }

    @PostMapping("/verify-email/resend")
    public ResponseEntity<OtpSentResponse> resendVerification(
            @Valid @RequestBody ResendEmailVerificationRequest request) {
        log.info("Resend verification for email: {}", request.getEmail());
        return ResponseEntity.ok(
                emailVerificationService.resendVerificationCode(request.getEmail()));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(
            @Valid @RequestBody LoginRequest request) {
        log.info("Login attempt for email: {}", request.getEmail());
        return ResponseEntity.ok(authenticationService.login(request));
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<OtpSentResponse> forgotPassword(
            @Valid @RequestBody ForgotPasswordRequest request) {
        log.info("Forgot password for email: {}", request.getEmail());
        return ResponseEntity.ok(authenticationService.forgotPassword(request));
    }

    @PostMapping("/reset-password")
    public ResponseEntity<AuthResponse> resetPassword(
            @Valid @RequestBody ResetPasswordRequest request) {
        log.info("Reset password attempt");
        return ResponseEntity.ok(authenticationService.resetPassword(request));
    }

    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("Auth service is running");
    }
}