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

    public void sendLoginOtp(String toEmail, String otp) {
        sendOtpEmail(
                toEmail,
                "Your Skytouch login code",
                "Your one-time login code is: " + otp
                        + "\n\nThis code expires in 10 minutes. Do not share it with anyone.");
    }

    public void sendEmailVerificationOtp(String toEmail, String otp) {
        sendOtpEmail(
                toEmail,
                "Verify your Skytouch email",
                "Your email verification code is: " + otp
                        + "\n\nEnter this code to activate your account. It expires in 10 minutes. Do not share it with anyone.");
    }

    private void sendOtpEmail(String toEmail, String subject, String text) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(authProperties.getEmailFrom());
        message.setTo(toEmail);
        message.setSubject(subject);
        message.setText(text);

        try {
            mailSender.send(message);
        } catch (Exception ex) {
            log.error("Failed to send email to {}", toEmail, ex);
            throw ex;
        }
    }
}
