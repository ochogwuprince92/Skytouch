package com.backend.Skytouch.authentication.service;

import com.backend.Skytouch.authentication.apimodel.AuthResponse;
import com.backend.Skytouch.authentication.apimodel.OtpSentResponse;
import com.backend.Skytouch.authentication.apimodel.RequestOtpRequest;
import com.backend.Skytouch.authentication.apimodel.VerifyOtpRequest;
import com.backend.Skytouch.authentication.config.AuthProperties;
import com.backend.Skytouch.authentication.repository.AuthenticationRepository;
import com.backend.Skytouch.authentication.security.PasswordVerifier;
import com.backend.Skytouch.common.enums.UserStatus;
import com.backend.Skytouch.common.exception.UnauthorizedException;
import com.backend.Skytouch.common.utils.EmailUtils;
import com.backend.Skytouch.user.entity.Users;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthenticationService {

    private final AuthenticationRepository authenticationRepository;
    private final PasswordEncoder passwordEncoder;
    private final PasswordVerifier passwordVerifier;
    private final OtpService otpService;
    private final SessionService sessionService;
    private final AuthProperties authProperties;

    @Transactional
    public OtpSentResponse requestOtp(RequestOtpRequest request) {
        Users user = authenticateCredentials(request.getEmail(), request.getPassword());
        otpService.sendLoginOtp(user.getId(), user.getEmail());

        return OtpSentResponse.builder()
                .message("OTP sent to " + EmailUtils.maskEmail(user.getEmail()))
                .expiresIn(authProperties.getOtp().getExpirationMs())
                .build();
    }

    @Transactional
    public AuthResponse verifyOtp(VerifyOtpRequest request) {
        Users user = authenticationRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new UnauthorizedException("Invalid or expired OTP"));

        assertAccountActive(user);
        otpService.verifyLoginOtp(user.getId(), request.getOtp());

        String sessionToken = sessionService.createSession(user.getId());

        return AuthResponse.builder()
                .accessToken(sessionToken)
                .expiresIn(authProperties.getSession().getExpirationMs())
                .userId(user.getId())
                .email(user.getEmail())
                .role(user.getRole())
                .build();
    }

    private Users authenticateCredentials(String email, String password) {
        Users user = authenticationRepository.findByEmail(email)
                .orElseThrow(() -> new UnauthorizedException("Invalid email or password"));

        assertAccountActive(user);

        if (!Boolean.TRUE.equals(user.getEmailVerified())) {
            throw new UnauthorizedException("Email verification required. Check your inbox or request a new code.");
        }

        if (!passwordVerifier.matches(password, user.getPassword(), passwordEncoder)) {
            throw new UnauthorizedException("Invalid email or password");
        }

        if (passwordVerifier.isLegacyPassword(user.getPassword())) {
            user.setPassword(passwordEncoder.encode(password));
            authenticationRepository.save(user);
        }

        return user;
    }

    private void assertAccountActive(Users user) {
        if (!Boolean.TRUE.equals(user.getActive())) {
            throw new UnauthorizedException("Account is inactive");
        }
        if (user.getStatus() == UserStatus.SUSPENDED) {
            throw new UnauthorizedException("Account is suspended");
        }
    }
}
