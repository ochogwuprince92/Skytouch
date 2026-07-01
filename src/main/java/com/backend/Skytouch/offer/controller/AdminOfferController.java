package com.backend.Skytouch.offer.controller;

import com.backend.Skytouch.offer.apimodel.OfferExpiryRunResponse;
import com.backend.Skytouch.offer.service.OfferExpiryService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/offers")
@RequiredArgsConstructor
public class AdminOfferController {

    private final OfferExpiryService offerExpiryService;

    @PostMapping("/expire-stale")
    @PreAuthorize("hasRole('ADMIN')")
    public OfferExpiryRunResponse expireStaleOffers() {
        return offerExpiryService.expireStaleOffers();
    }
}
