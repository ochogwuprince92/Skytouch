package com.backend.Skytouch.authentication.service;

import com.backend.Skytouch.authentication.config.AuthProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    private final JavaMailSender mailSender;
    private final AuthProperties authProperties;

    public void sendEmailVerificationOtp(String toEmail, String otp) {
        sendOtpEmail(
                toEmail,
                "Verify your Skytouch email",
                "Your email verification code is: " + otp
                        + "\n\nEnter this code to activate your account. It expires in 10 minutes. Do not share it with anyone.");
    }

    public void sendPasswordResetOtp(String toEmail, String otp) {
        sendOtpEmail(
                toEmail,
                "Reset your Skytouch password",
                "Your password reset code is: " + otp
                        + "\n\nThis code expires in 10 minutes. If you didn't request this, ignore this email. Do not share it with anyone.");
    }

    private void sendOtpEmail(String toEmail, String subject, String text) {
        if (!authProperties.isMailSendEnabled()) {
            log.info("Mail send disabled; skipped email to {} subject={}", toEmail, subject);
            return;
        }

        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(authProperties.getEmailFrom());
        message.setTo(toEmail);
        message.setSubject(subject);
        message.setText(text);

        try {
            mailSender.send(message);
            log.info("Email sent to {} subject={}", toEmail, subject);
        } catch (Exception ex) {
            log.error("Failed to send email to {}", toEmail, ex);
            if (authProperties.isLogOtp()) {
                log.warn("Continuing without email because APP_LOG_OTP is enabled; use the OTP from the logs above");
                return;
            }
            throw ex;
        }
    }
}