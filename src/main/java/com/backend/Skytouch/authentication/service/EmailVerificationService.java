package com.backend.Skytouch.authentication.service;

import com.backend.Skytouch.authentication.apimodel.EmailVerifiedResponse;
import com.backend.Skytouch.authentication.apimodel.OtpSentResponse;
import com.backend.Skytouch.authentication.apimodel.VerifyOtpRequest;
import com.backend.Skytouch.authentication.config.AuthProperties;
import com.backend.Skytouch.authentication.repository.AuthenticationRepository;
import com.backend.Skytouch.common.enums.UserStatus;
import com.backend.Skytouch.common.exception.BadRequestException;
import com.backend.Skytouch.common.exception.ResourceNotFoundException;
import com.backend.Skytouch.common.exception.UnauthorizedException;
import com.backend.Skytouch.common.utils.EmailUtils;
import com.backend.Skytouch.user.entity.Users;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class EmailVerificationService {

    private final AuthenticationRepository authenticationRepository;
    private final OtpService otpService;
    private final AuthProperties authProperties;

    @Transactional
    public OtpSentResponse sendVerificationCode(Users user) {
        assertCanVerify(user);
        otpService.sendEmailVerificationOtp(user.getId(), user.getEmail());
        return buildOtpSentResponse(user.getEmail());
    }

    @Transactional
    public OtpSentResponse resendVerificationCode(String email) {
        Users user = authenticationRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("No account found for email: " + email));

        assertCanVerify(user);
        otpService.sendEmailVerificationOtp(user.getId(), user.getEmail());
        return buildOtpSentResponse(user.getEmail());
    }

    @Transactional
    public EmailVerifiedResponse verifyEmail(VerifyOtpRequest request) {
        Users user = authenticationRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new UnauthorizedException("Invalid or expired OTP"));

        if (Boolean.TRUE.equals(user.getEmailVerified())) {
            throw new BadRequestException("Email is already verified");
        }

        assertAccountActive(user);
        otpService.verifyEmailVerificationOtp(user.getId(), request.getOtp());

        user.setEmailVerified(true);
        if (user.getStatus() == UserStatus.PENDING) {
            user.setStatus(UserStatus.ACTIVE);
        }
        authenticationRepository.save(user);

        return EmailVerifiedResponse.builder()
                .message("Email verified successfully")
                .emailVerified(true)
                .status(user.getStatus())
                .build();
    }

    private void assertCanVerify(Users user) {
        assertAccountActive(user);

        if (Boolean.TRUE.equals(user.getEmailVerified())) {
            throw new BadRequestException("Email is already verified");
        }
    }

    private void assertAccountActive(Users user) {
        if (!Boolean.TRUE.equals(user.getActive())) {
            throw new UnauthorizedException("Account is inactive");
        }
        if (user.getStatus() == UserStatus.SUSPENDED) {
            throw new UnauthorizedException("Account is suspended");
        }
    }

    private OtpSentResponse buildOtpSentResponse(String email) {
        return OtpSentResponse.builder()
                .message("Verification code sent to " + EmailUtils.maskEmail(email))
                .expiresIn(authProperties.getOtp().getExpirationMs())
                .build();
    }
}
