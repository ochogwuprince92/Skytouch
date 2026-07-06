package com.backend.Skytouch.offer.apimodel;

import com.backend.Skytouch.common.enums.OfferStatus;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Builder
public class OfferResponse {

    private final UUID id;
    private final UUID applicationId;
    private final String jobTitle;
    private final String companyName;
    private final String seekerName;
    private final Long salaryAmount;
    private final String salaryCurrency;
    private final LocalDate startDate;
    private final String terms;
    private final OfferStatus status;
    private final LocalDateTime offeredAt;
    private final LocalDateTime expiresAt;
    private final LocalDateTime respondedAt;
}
