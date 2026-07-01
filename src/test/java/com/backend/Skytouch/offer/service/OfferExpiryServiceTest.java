package com.backend.Skytouch.offer.service;

import com.backend.Skytouch.application.entity.JobApplication;
import com.backend.Skytouch.application.repository.JobApplicationRepository;
import com.backend.Skytouch.common.enums.ApplicationStatus;
import com.backend.Skytouch.common.enums.OfferStatus;
import com.backend.Skytouch.job.entity.Job;
import com.backend.Skytouch.notification.service.NotificationService;
import com.backend.Skytouch.offer.entity.JobOffer;
import com.backend.Skytouch.offer.repository.JobOfferRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OfferExpiryServiceTest {

    @Mock private JobOfferRepository offerRepository;
    @Mock private JobApplicationRepository applicationRepository;
    @Mock private NotificationService notificationService;

    @InjectMocks
    private OfferExpiryService offerExpiryService;

    @Test
    void expireStaleOffers_marksOffersExpiredAndNotifies() {
        Job job = Job.builder().id(UUID.randomUUID()).title("Engineer").build();
        JobApplication application = JobApplication.builder()
                .id(UUID.randomUUID())
                .job(job)
                .status(ApplicationStatus.OFFER_EXTENDED)
                .build();
        JobOffer offer = JobOffer.builder()
                .id(UUID.randomUUID())
                .application(application)
                .status(OfferStatus.PENDING)
                .expiresAt(LocalDateTime.now().minusHours(1))
                .build();

        when(offerRepository.findByStatusAndExpiresAtBefore(eq(OfferStatus.PENDING), any(LocalDateTime.class)))
                .thenReturn(List.of(offer));
        when(offerRepository.save(offer)).thenReturn(offer);
        when(applicationRepository.save(application)).thenReturn(application);

        var result = offerExpiryService.expireStaleOffers();

        assertThat(result.getOffersExpired()).isEqualTo(1);
        assertThat(offer.getStatus()).isEqualTo(OfferStatus.EXPIRED);
        assertThat(application.getStatus()).isEqualTo(ApplicationStatus.OFFER_DECLINED);
        verify(notificationService).notifyOnOfferExpired(offer);
    }
}
