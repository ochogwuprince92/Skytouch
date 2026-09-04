package com.backend.Skytouch.payment.controller;

import com.backend.Skytouch.common.config.PaystackConfig;
import com.backend.Skytouch.payment.entity.Payment;
import com.backend.Skytouch.payment.enums.PaymentStatus;
import com.backend.Skytouch.payment.repository.PaymentRepository;
import com.backend.Skytouch.subscription.entity.EmployerSubscription;
import com.backend.Skytouch.subscription.enums.SubscriptionStatus;
import com.backend.Skytouch.subscription.repository.SubscriptionRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/webhooks")
@RequiredArgsConstructor
@Slf4j
public class WebhookController {

    private final PaystackConfig paystackConfig;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final PaymentRepository paymentRepository;
    private final SubscriptionRepository subscriptionRepository;

    @PostMapping("/paystack")
    public ResponseEntity<String> handlePaystackWebhook(
            @RequestBody String payload,
            @RequestHeader("x-paystack-signature") String signature,
            HttpServletRequest request) {
        
        try {
            // Verify webhook signature
            String computedSignature = computeHmacSha512(payload, paystackConfig.getSecretKey());
            
            if (!computedSignature.equals(signature)) {
                log.warn("Invalid webhook signature");
                return ResponseEntity.status(401).body("Invalid signature");
            }

            // Parse webhook event
            JsonNode rootNode = objectMapper.readTree(payload);
            String event = rootNode.path("event").asText();
            JsonNode data = rootNode.path("data");

            log.info("Received Paystack webhook event: {}", event);

            // Handle different event types
            switch (event) {
                case "charge.success":
                    handleChargeSuccess(data);
                    break;
                case "charge.failed":
                    handleChargeFailed(data);
                    break;
                case "transfer.success":
                    handleTransferSuccess(data);
                    break;
                case "transfer.failed":
                    handleTransferFailed(data);
                    break;
                case "refund.processed":
                    handleRefundProcessed(data);
                    break;
                default:
                    log.info("Unhandled webhook event: {}", event);
            }

            return ResponseEntity.ok("Webhook received");

        } catch (Exception e) {
            log.error("Error processing webhook", e);
            return ResponseEntity.status(500).body("Error processing webhook");
        }
    }

    private void handleChargeSuccess(JsonNode data) {
        String reference = data.path("reference").asText();
        String email = data.path("customer").path("email").asText();
        String amount = data.path("amount").asText();
        String gatewayResponse = data.path("gateway_response").asText();
        String paidAt = data.path("paid_at").asText();
        
        log.info("Payment successful - Reference: {}, Email: {}, Amount: {}", reference, email, amount);
        
        try {
            Optional<Payment> paymentOpt = paymentRepository.findByReference(reference);
            if (paymentOpt.isEmpty()) {
                log.warn("Payment not found for reference: {}", reference);
                return;
            }
            
            Payment payment = paymentOpt.get();
            payment.setStatus(PaymentStatus.SUCCESS);
            payment.setGatewayResponse(gatewayResponse);
            payment.setPaidAt(parseDateTime(paidAt));
            paymentRepository.save(payment);
            
            // Extract companyId from metadata and activate subscription
            JsonNode metadata = data.path("metadata");
            if (metadata.has("companyId")) {
                UUID companyId = UUID.fromString(metadata.get("companyId").asText());
                activateSubscriptionForPayment(companyId);
            }
            
            log.info("Successfully updated payment status to SUCCESS for reference: {}", reference);
        } catch (Exception e) {
            log.error("Error handling charge success for reference: {}", reference, e);
        }
    }

    private void handleChargeFailed(JsonNode data) {
        String reference = data.path("reference").asText();
        String email = data.path("customer").path("email").asText();
        String gatewayResponse = data.path("gateway_response").asText();
        
        log.info("Payment failed - Reference: {}, Email: {}", reference, email);
        
        try {
            Optional<Payment> paymentOpt = paymentRepository.findByReference(reference);
            if (paymentOpt.isEmpty()) {
                log.warn("Payment not found for reference: {}", reference);
                return;
            }
            
            Payment payment = paymentOpt.get();
            payment.setStatus(PaymentStatus.FAILED);
            payment.setGatewayResponse(gatewayResponse);
            paymentRepository.save(payment);
            
            // Mark subscription as failed if it exists
            JsonNode metadata = data.path("metadata");
            if (metadata.has("companyId")) {
                UUID companyId = UUID.fromString(metadata.get("companyId").asText());
                markSubscriptionAsFailed(companyId);
            }
            
            log.info("Successfully updated payment status to FAILED for reference: {}", reference);
        } catch (Exception e) {
            log.error("Error handling charge failed for reference: {}", reference, e);
        }
    }

    private void handleTransferSuccess(JsonNode data) {
        String reference = data.path("reference").asText();
        String amount = data.path("amount").asText();
        
        log.info("Transfer successful - Reference: {}, Amount: {}", reference, amount);
        // Transfer logic not currently used in this application
    }

    private void handleTransferFailed(JsonNode data) {
        String reference = data.path("reference").asText();
        
        log.info("Transfer failed - Reference: {}", reference);
        // Transfer logic not currently used in this application
    }

    private void handleRefundProcessed(JsonNode data) {
        String reference = data.path("reference").asText();
        String amount = data.path("amount").asText();
        
        log.info("Refund processed - Reference: {}, Amount: {}", reference, amount);
        
        try {
            Optional<Payment> paymentOpt = paymentRepository.findByReference(reference);
            if (paymentOpt.isEmpty()) {
                log.warn("Payment not found for reference: {}", reference);
                return;
            }
            
            Payment payment = paymentOpt.get();
            // If refund processed, deactivate the associated subscription
            JsonNode metadata = data.path("metadata");
            if (metadata.has("companyId")) {
                UUID companyId = UUID.fromString(metadata.get("companyId").asText());
                deactivateSubscriptionForRefund(companyId);
            }
            
            log.info("Successfully handled refund for reference: {}", reference);
        } catch (Exception e) {
            log.error("Error handling refund for reference: {}", reference, e);
        }
    }

    private void activateSubscriptionForPayment(UUID companyId) {
        try {
            Optional<EmployerSubscription> subscriptionOpt = subscriptionRepository.findByCompanyId(companyId);
            if (subscriptionOpt.isPresent()) {
                EmployerSubscription subscription = subscriptionOpt.get();
                if (subscription.getStatus() == SubscriptionStatus.PENDING) {
                    subscription.setStatus(SubscriptionStatus.ACTIVE);
                    subscription.setStartDate(LocalDateTime.now());
                    subscriptionRepository.save(subscription);
                    log.info("Activated subscription for company: {}", companyId);
                }
            }
        } catch (Exception e) {
            log.error("Error activating subscription for company: {}", companyId, e);
        }
    }

    private void markSubscriptionAsFailed(UUID companyId) {
        try {
            Optional<EmployerSubscription> subscriptionOpt = subscriptionRepository.findByCompanyId(companyId);
            if (subscriptionOpt.isPresent()) {
                EmployerSubscription subscription = subscriptionOpt.get();
                if (subscription.getStatus() == SubscriptionStatus.PENDING) {
                    subscription.setStatus(SubscriptionStatus.CANCELLED);
                    subscriptionRepository.save(subscription);
                    log.info("Marked subscription as failed/cancelled for company: {}", companyId);
                }
            }
        } catch (Exception e) {
            log.error("Error marking subscription as failed for company: {}", companyId, e);
        }
    }

    private void deactivateSubscriptionForRefund(UUID companyId) {
        try {
            Optional<EmployerSubscription> subscriptionOpt = subscriptionRepository.findByCompanyId(companyId);
            if (subscriptionOpt.isPresent()) {
                EmployerSubscription subscription = subscriptionOpt.get();
                subscription.setStatus(SubscriptionStatus.CANCELLED);
                subscriptionRepository.save(subscription);
                log.info("Deactivated subscription due to refund for company: {}", companyId);
            }
        } catch (Exception e) {
            log.error("Error deactivating subscription for company: {}", companyId, e);
        }
    }

    private String computeHmacSha512(String data, String secret) throws Exception {
        Mac sha512Hmac = Mac.getInstance("HmacSHA512");
        SecretKeySpec secretKey = new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA512");
        sha512Hmac.init(secretKey);
        byte[] hashBytes = sha512Hmac.doFinal(data.getBytes(StandardCharsets.UTF_8));
        return Base64.getEncoder().encodeToString(hashBytes);
    }

    private LocalDateTime parseDateTime(String dateTime) {
        if (dateTime == null || dateTime.isEmpty()) {
            return null;
        }
        try {
            return LocalDateTime.parse(dateTime, java.time.format.DateTimeFormatter.ISO_DATE_TIME);
        } catch (Exception e) {
            log.warn("Failed to parse datetime: {}", dateTime);
            return null;
        }
    }
}
