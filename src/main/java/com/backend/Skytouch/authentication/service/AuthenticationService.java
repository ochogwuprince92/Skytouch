package com.backend.Skytouch.authentication.service;

import com.backend.Skytouch.authentication.apimodel.*;
import com.backend.Skytouch.authentication.config.AuthProperties;
import com.backend.Skytouch.authentication.repository.AuthenticationRepository;
import com.backend.Skytouch.authentication.security.PasswordVerifier;
import com.backend.Skytouch.common.enums.OtpPurpose;
import com.backend.Skytouch.common.enums.UserRole;
import com.backend.Skytouch.common.enums.UserStatus;
import com.backend.Skytouch.common.enums.UserType;
import com.backend.Skytouch.common.exception.BadRequestException;
import com.backend.Skytouch.common.exception.ConflictException;
import com.backend.Skytouch.common.exception.ResourceNotFoundException;
import com.backend.Skytouch.common.exception.UnauthorizedException;
import com.backend.Skytouch.common.mapper.EmployerMapper;
import com.backend.Skytouch.common.mapper.JobSeekerMapper;
import com.backend.Skytouch.common.utils.EmailUtils;
import com.backend.Skytouch.employer.entity.Employer;
import com.backend.Skytouch.employer.repository.EmployerRepository;
import com.backend.Skytouch.jobseeker.entity.JobSeeker;
import com.backend.Skytouch.jobseeker.repository.JobSeekerRepository;
import com.backend.Skytouch.user.entity.Users;
import com.backend.Skytouch.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthenticationService {

    private final AuthenticationRepository authenticationRepository;
    private final UserRepository userRepository;
    private final JobSeekerRepository jobSeekerRepository;
    private final EmployerRepository employerRepository;
    private final JobSeekerMapper jobSeekerMapper;
    private final EmployerMapper employerMapper;
    private final PasswordEncoder passwordEncoder;
    private final PasswordVerifier passwordVerifier;
    private final OtpService otpService;
    private final SessionService sessionService;
    private final EmailVerificationService emailVerificationService;
    private final AuthProperties authProperties;

    // ─── Register ─────────────────────────────────────────────────────────────

    @Transactional
    public RegisterResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new ConflictException("Email already registered: " + request.getEmail());
        }

        if (!request.getPassword().equals(request.getConfirmPassword())) {
            throw new BadRequestException("Passwords do not match");
        }

        UserRole role = request.getUserType().toUserRole();

        Users user = Users.builder()
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(role)
                .status(UserStatus.PENDING)
                .build();

        Users savedUser = userRepository.save(user);

        if (request.getUserType() == UserType.JOB_SEEKER) {
            JobSeeker profile = jobSeekerMapper.toEntity(request, savedUser);
            jobSeekerRepository.save(profile);
        } else {
            Employer profile = employerMapper.toEntity(request, savedUser);
            employerRepository.save(profile);
        }

        var verification = emailVerificationService.sendVerificationCode(savedUser);

        return toRegisterResponse(savedUser, verification);
    }

    // ─── Login ────────────────────────────────────────────────────────────────

    @Transactional
    public AuthResponse login(LoginRequest request) {
        Users user = authenticationRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new UnauthorizedException("Invalid email or password"));

        assertAccountActive(user);

        if (!Boolean.TRUE.equals(user.getEmailVerified())) {
            throw new UnauthorizedException(
                    "Email not verified. Check your inbox or request a new code.");
        }

        if (!passwordVerifier.matches(request.getPassword(), user.getPassword(), passwordEncoder)) {
            throw new UnauthorizedException("Invalid email or password");
        }

        if (passwordVerifier.isLegacyPassword(user.getPassword())) {
            user.setPassword(passwordEncoder.encode(request.getPassword()));
            authenticationRepository.save(user);
        }

        String sessionToken = sessionService.createSession(user.getId());

        return AuthResponse.builder()
                .accessToken(sessionToken)
                .expiresIn(authProperties.getSession().getExpirationMs())
                .userId(user.getId())
                .email(user.getEmail())
                .role(user.getRole())
                .build();
    }

    // ─── Forgot password ──────────────────────────────────────────────────────

    @Transactional
    public OtpSentResponse forgotPassword(ForgotPasswordRequest request) {
        Users user = authenticationRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "No account found for email: " + request.getEmail()));

        assertAccountActive(user);
        otpService.sendOtp(user.getId(), user.getEmail(), OtpPurpose.PASSWORD_RESET);

        return OtpSentResponse.builder()
                .message("Password reset code sent to " + EmailUtils.maskEmail(user.getEmail()))
                .expiresIn(authProperties.getOtp().getExpirationMs())
                .build();
    }

    // ─── Reset password ───────────────────────────────────────────────────────

    @Transactional
    public AuthResponse resetPassword(ResetPasswordRequest request) {
        Users user = authenticationRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new UnauthorizedException("Invalid or expired OTP"));

        assertAccountActive(user);
        otpService.verifyOtp(user.getId(), request.getOtp(), OtpPurpose.PASSWORD_RESET);

        if (request.getNewPassword() == null || request.getNewPassword().length() < 8) {
            throw new BadRequestException("Password must be at least 8 characters");
        }

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        authenticationRepository.save(user);

        String sessionToken = sessionService.createSession(user.getId());

        return AuthResponse.builder()
                .accessToken(sessionToken)
                .expiresIn(authProperties.getSession().getExpirationMs())
                .userId(user.getId())
                .email(user.getEmail())
                .role(user.getRole())
                .build();
    }

    // ─── Logout ───────────────────────────────────────────────────────────────

    @Transactional
    public LogoutResponse logout(String accessToken) {
        sessionService.revokeSession(accessToken);
        return LogoutResponse.builder()
                .message("Logged out successfully")
                .build();
    }

    @Transactional
    public MessageResponse changePassword(String email, ChangePasswordRequest request) {
        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            throw new BadRequestException("Passwords do not match");
        }

        Users user = authenticationRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + email));
        assertAccountActive(user);

        if (!passwordVerifier.matches(request.getCurrentPassword(), user.getPassword(), passwordEncoder)) {
            throw new UnauthorizedException("Current password is incorrect");
        }

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        authenticationRepository.save(user);
        sessionService.revokeAllSessions(user.getId());

        return MessageResponse.builder()
                .message("Password updated successfully")
                .build();
    }

    @Transactional
    public MessageResponse deactivateAccount(String email, DeactivateAccountRequest request) {
        Users user = authenticationRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + email));

        if (!passwordVerifier.matches(request.getPassword(), user.getPassword(), passwordEncoder)) {
            throw new UnauthorizedException("Invalid password");
        }

        user.setActive(false);
        user.setStatus(UserStatus.SUSPENDED);
        authenticationRepository.save(user);
        sessionService.revokeAllSessions(user.getId());

        return MessageResponse.builder()
                .message("Account deactivated successfully")
                .build();
    }

    // ─── Private helpers ──────────────────────────────────────────────────────

    private void assertAccountActive(Users user) {
        if (!Boolean.TRUE.equals(user.getActive())) {
            throw new UnauthorizedException("Account is inactive");
        }
        if (user.getStatus() == UserStatus.SUSPENDED) {
            throw new UnauthorizedException("Account is suspended");
        }
    }

    private RegisterResponse toRegisterResponse(Users user, OtpSentResponse verification) {
        return RegisterResponse.builder()
                .id(user.getId())
                .email(user.getEmail())
                .role(user.getRole())
                .status(user.getStatus())
                .emailVerified(user.getEmailVerified())
                .active(user.getActive())
                .createdAt(user.getCreatedAt())
                .verificationMessage(verification.getMessage())
                .verificationExpiresIn(verification.getExpiresIn())
                .build();
    }
}