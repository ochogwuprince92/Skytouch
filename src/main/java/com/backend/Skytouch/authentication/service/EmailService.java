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

    public void sendApplicationSubmittedConfirmation(String toEmail, String jobTitle, String companyName) {
        sendPlainEmail(
                toEmail,
                "Application submitted: " + jobTitle,
                "Your application for \"" + jobTitle + "\" at " + companyName
                        + " has been submitted successfully.\n\n"
                        + "You can track its status in your Skytouch dashboard.");
    }

    public void sendNewApplicationAlert(String toEmail, String seekerName, String jobTitle) {
        sendPlainEmail(
                toEmail,
                "New application: " + jobTitle,
                seekerName + " has applied for \"" + jobTitle + "\".\n\n"
                        + "Log in to Skytouch to review the application.");
    }

    public void sendApplicationStatusUpdate(String toEmail, String jobTitle, String statusLabel) {
        sendPlainEmail(
                toEmail,
                "Application update: " + jobTitle,
                "Your application for \"" + jobTitle + "\" is now " + statusLabel + ".\n\n"
                        + "Log in to Skytouch for more details.");
    }

    public void sendInterviewScheduled(String toEmail, String jobTitle, String scheduledAt) {
        sendPlainEmail(
                toEmail,
                "Interview scheduled: " + jobTitle,
                "Your interview for \"" + jobTitle + "\" is scheduled for " + scheduledAt + ".\n\n"
                        + "Log in to Skytouch for details.");
    }

    public void sendInterviewUpdated(String toEmail, String jobTitle, String details) {
        sendPlainEmail(
                toEmail,
                "Interview update: " + jobTitle,
                "Your interview for \"" + jobTitle + "\" has been updated.\n\n" + details);
    }

    public void sendNewMessageAlert(String toEmail, String jobTitle) {
        sendPlainEmail(
                toEmail,
                "New message: " + jobTitle,
                "You have a new message regarding your application for \"" + jobTitle + "\".\n\n"
                        + "Log in to Skytouch to read and reply.");
    }

    public void sendOfferExtended(String toEmail, String jobTitle, String companyName) {
        sendPlainEmail(
                toEmail,
                "Job offer: " + jobTitle,
                "Congratulations! You received an offer for \"" + jobTitle + "\" at " + companyName + ".\n\n"
                        + "Log in to Skytouch to review and respond.");
    }

    public void sendOfferAccepted(String toEmail, String seekerName, String jobTitle) {
        sendPlainEmail(
                toEmail,
                "Offer accepted: " + jobTitle,
                seekerName + " accepted your offer for \"" + jobTitle + "\".\n\n"
                        + "The position has been filled.");
    }

    public void sendOfferDeclined(String toEmail, String seekerName, String jobTitle) {
        sendPlainEmail(
                toEmail,
                "Offer declined: " + jobTitle,
                seekerName + " declined your offer for \"" + jobTitle + "\".");
    }

    public void sendHiredConfirmation(String toEmail, String jobTitle) {
        sendPlainEmail(
                toEmail,
                "You're hired: " + jobTitle,
                "Congratulations! You are now hired for \"" + jobTitle + "\".\n\n"
                        + "Your employer will be in touch with next steps.");
    }

    public void sendJobAlertMatch(String toEmail, String jobTitle, String companyName) {
        sendPlainEmail(
                toEmail,
                "New job alert: " + jobTitle,
                "A new job \"" + jobTitle + "\" at " + companyName
                        + " matches your saved alert criteria.\n\n"
                        + "Log in to Skytouch to view and apply.");
    }

    public void sendJobAlertDigest(String toEmail, java.util.List<String> jobSummaries) {
        StringBuilder body = new StringBuilder("Here are new jobs matching your alerts:\n\n");
        for (String summary : jobSummaries) {
            body.append("- ").append(summary).append('\n');
        }
        body.append("\nLog in to Skytouch to view and apply.");
        sendPlainEmail(toEmail, "Your Skytouch job alert digest", body.toString());
    }

    private void sendOtpEmail(String toEmail, String subject, String text) {
        sendPlainEmail(toEmail, subject, text, true);
    }

    private void sendPlainEmail(String toEmail, String subject, String text) {
        sendPlainEmail(toEmail, subject, text, false);
    }

    private void sendPlainEmail(String toEmail, String subject, String text, boolean otpEmail) {
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
            if (otpEmail && authProperties.isLogOtp()) {
                log.warn("Continuing without email because APP_LOG_OTP is enabled; use the OTP from the logs above");
                return;
            }
            log.warn("Notification email failed for {}; in-app notification was still saved", toEmail);
        }
    }
}