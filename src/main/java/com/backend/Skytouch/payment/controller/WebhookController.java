package com.backend.Skytouch.payment.controller;

import com.backend.Skytouch.common.config.PaystackConfig;
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
import java.util.Base64;

@RestController
@RequestMapping("/api/v1/webhooks")
@RequiredArgsConstructor
@Slf4j
public class WebhookController {

    private final PaystackConfig paystackConfig;
    private final ObjectMapper objectMapper = new ObjectMapper();

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
        
        log.info("Payment successful - Reference: {}, Email: {}, Amount: {}", reference, email, amount);
        
        // TODO: Implement your business logic here
        // - Update payment status in database
        // - Send confirmation email
        // - Update subscription/job posting status
        // - Trigger any post-payment actions
    }

    private void handleChargeFailed(JsonNode data) {
        String reference = data.path("reference").asText();
        String email = data.path("customer").path("email").asText();
        
        log.info("Payment failed - Reference: {}, Email: {}", reference, email);
        
        // TODO: Implement your business logic here
        // - Update payment status in database
        // - Send failure notification
        // - Handle retry logic if needed
    }

    private void handleTransferSuccess(JsonNode data) {
        String reference = data.path("reference").asText();
        String amount = data.path("amount").asText();
        
        log.info("Transfer successful - Reference: {}, Amount: {}", reference, amount);
        
        // TODO: Implement transfer success logic
    }

    private void handleTransferFailed(JsonNode data) {
        String reference = data.path("reference").asText();
        
        log.info("Transfer failed - Reference: {}", reference);
        
        // TODO: Implement transfer failure logic
    }

    private void handleRefundProcessed(JsonNode data) {
        String reference = data.path("reference").asText();
        String amount = data.path("amount").asText();
        
        log.info("Refund processed - Reference: {}, Amount: {}", reference, amount);
        
        // TODO: Implement refund logic
    }

    private String computeHmacSha512(String data, String secret) throws Exception {
        Mac sha512Hmac = Mac.getInstance("HmacSHA512");
        SecretKeySpec secretKey = new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA512");
        sha512Hmac.init(secretKey);
        byte[] hashBytes = sha512Hmac.doFinal(data.getBytes(StandardCharsets.UTF_8));
        return Base64.getEncoder().encodeToString(hashBytes);
    }
}
