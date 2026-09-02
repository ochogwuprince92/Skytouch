package com.backend.Skytouch.offer.controller;

import com.backend.Skytouch.authentication.security.SecurityUtils;
import com.backend.Skytouch.common.apimodel.PageResponse;
import com.backend.Skytouch.offer.apimodel.OfferCreateRequest;
import com.backend.Skytouch.offer.apimodel.OfferResponse;
import com.backend.Skytouch.offer.service.OfferService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class OfferController {

    private final OfferService offerService;

    @PostMapping("/api/applications/{applicationId}/offers")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('EMPLOYER')")
    public OfferResponse extendOffer(
            @PathVariable UUID applicationId,
            @Valid @RequestBody OfferCreateRequest request) {
        return offerService.extendOffer(SecurityUtils.getCurrentUser().getEmail(), applicationId, request);
    }

    @GetMapping("/api/applications/{applicationId}/offers")
    @PreAuthorize("hasAnyRole('EMPLOYER', 'JOB_SEEKER')")
    public OfferResponse getOfferForApplication(@PathVariable UUID applicationId) {
        return offerService.findForApplication(SecurityUtils.getCurrentUser().getEmail(), applicationId);
    }

    @PostMapping("/api/offers/{id}/accept")
    @PreAuthorize("hasRole('JOB_SEEKER')")
    public OfferResponse acceptOffer(@PathVariable UUID id) {
        return offerService.acceptOffer(SecurityUtils.getCurrentUser().getEmail(), id);
    }

    @PostMapping("/api/offers/{id}/decline")
    @PreAuthorize("hasRole('JOB_SEEKER')")
    public OfferResponse declineOffer(@PathVariable UUID id) {
        return offerService.declineOffer(SecurityUtils.getCurrentUser().getEmail(), id);
    }

    @GetMapping("/api/offers/me")
    @PreAuthorize("hasRole('JOB_SEEKER')")
    public PageResponse<OfferResponse> getMyPendingOffers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return offerService.findMyPendingOffers(SecurityUtils.getCurrentUser().getEmail(), page, size);
    }
}
