package com.backend.Skytouch.offer.scheduler;

import com.backend.Skytouch.offer.config.OfferExpiryProperties;
import com.backend.Skytouch.offer.service.OfferExpiryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class OfferExpiryScheduler {

    private final OfferExpiryService offerExpiryService;
    private final OfferExpiryProperties properties;

    @Scheduled(cron = "${app.offer.expiry.cron:0 0 * * * *}")
    public void runScheduledExpiry() {
        if (!properties.isEnabled()) {
            return;
        }
        log.debug("Running scheduled offer expiry check");
        offerExpiryService.expireStaleOffers();
    }
}
