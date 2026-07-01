package com.backend.Skytouch.offer.service;

import com.backend.Skytouch.application.entity.JobApplication;
import com.backend.Skytouch.application.repository.JobApplicationRepository;
import com.backend.Skytouch.common.enums.ApplicationStatus;
import com.backend.Skytouch.common.enums.OfferStatus;
import com.backend.Skytouch.notification.service.NotificationService;
import com.backend.Skytouch.offer.apimodel.OfferExpiryRunResponse;
import com.backend.Skytouch.offer.entity.JobOffer;
import com.backend.Skytouch.offer.repository.JobOfferRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class OfferExpiryService {

    private final JobOfferRepository offerRepository;
    private final JobApplicationRepository applicationRepository;
    private final NotificationService notificationService;

    @Transactional
    public OfferExpiryRunResponse expireStaleOffers() {
        List<JobOffer> staleOffers = offerRepository.findByStatusAndExpiresAtBefore(
                OfferStatus.PENDING, LocalDateTime.now());

        for (JobOffer offer : staleOffers) {
            offer.setStatus(OfferStatus.EXPIRED);
            JobApplication application = offer.getApplication();
            application.setStatus(ApplicationStatus.OFFER_DECLINED);
            offerRepository.save(offer);
            applicationRepository.save(application);
            notificationService.notifyOnOfferExpired(offer);
        }

        if (!staleOffers.isEmpty()) {
            log.info("Expired {} stale job offers", staleOffers.size());
        }

        return OfferExpiryRunResponse.builder()
                .offersExpired(staleOffers.size())
                .build();
    }
}
