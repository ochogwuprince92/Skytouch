package com.backend.Skytouch.authentication.controller;

import com.backend.Skytouch.authentication.apimodel.AuthResponse;
import com.backend.Skytouch.authentication.apimodel.EmailVerifiedResponse;
import com.backend.Skytouch.authentication.apimodel.OtpSentResponse;
import com.backend.Skytouch.authentication.apimodel.RequestOtpRequest;
import com.backend.Skytouch.authentication.apimodel.ResendEmailVerificationRequest;
import com.backend.Skytouch.authentication.apimodel.VerifyOtpRequest;
import com.backend.Skytouch.authentication.service.AuthenticationService;
import com.backend.Skytouch.authentication.service.EmailVerificationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthenticationController {

    private final AuthenticationService authenticationService;
    private final EmailVerificationService emailVerificationService;

    @PostMapping("/otp/request")
    public OtpSentResponse requestOtp(@Valid @RequestBody RequestOtpRequest request) {
        return authenticationService.requestOtp(request);
    }

    @PostMapping("/otp/verify")
    public AuthResponse verifyOtp(@Valid @RequestBody VerifyOtpRequest request) {
        return authenticationService.verifyOtp(request);
    }

    @PostMapping("/email/verify")
    public EmailVerifiedResponse verifyEmail(@Valid @RequestBody VerifyOtpRequest request) {
        return emailVerificationService.verifyEmail(request);
    }

    @PostMapping("/email/resend")
    public OtpSentResponse resendEmailVerification(@Valid @RequestBody ResendEmailVerificationRequest request) {
        return emailVerificationService.resendVerificationCode(request.getEmail());
    }
}
