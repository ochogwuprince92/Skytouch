package com.backend.Skytouch.payment.controller;

import com.backend.Skytouch.common.config.PaystackConfig;
import com.backend.Skytouch.payment.apimodel.PaymentInitializeRequest;
import com.backend.Skytouch.payment.apimodel.PaymentInitializeResponse;
import com.backend.Skytouch.payment.apimodel.PaymentVerifyResponse;
import com.backend.Skytouch.payment.service.PaymentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.view.RedirectView;

@RestController
@RequestMapping("/api/v1/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;
    private final PaystackConfig paystackConfig;

    @Value("${app.frontend.url}")
    private String frontendUrl;

    @PostMapping("/initialize")
    public ResponseEntity<PaymentInitializeResponse> initializePayment(@Valid @RequestBody PaymentInitializeRequest request) {
        if (request.getCallbackUrl() == null || request.getCallbackUrl().isEmpty()) {
            request.setCallbackUrl(paystackConfig.getCallbackUrl());
        }
        PaymentInitializeResponse response = paymentService.initializePayment(request);
        if (response.isStatus()) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }

    @GetMapping("/verify/{reference}")
    public ResponseEntity<PaymentVerifyResponse> verifyPayment(@PathVariable String reference) {
        PaymentVerifyResponse response = paymentService.verifyPayment(reference);
        if (response.isStatus()) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }

    @GetMapping("/callback")
    public RedirectView paymentCallback(@RequestParam("reference") String reference) {
        PaymentVerifyResponse response = paymentService.verifyPayment(reference);
        String redirectUrl;
        
        if (response.isStatus()) {
            redirectUrl = frontendUrl + "/payment/success?reference=" + reference;
        } else {
            redirectUrl = frontendUrl + "/payment/failure?reference=" + reference;
        }
        
        return new RedirectView(redirectUrl);
    }
}
