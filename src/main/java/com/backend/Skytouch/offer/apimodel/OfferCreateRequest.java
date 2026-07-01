package com.backend.Skytouch.offer.apimodel;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.Min;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
public class OfferCreateRequest {

    @Min(0)
    private Long salaryAmount;

    private String salaryCurrency = "NGN";

    private LocalDate startDate;

    private String terms;

    @Future
    private LocalDateTime expiresAt;
}
