package com.backend.Skytouch.common.mapper;

import com.backend.Skytouch.application.entity.JobApplication;
import com.backend.Skytouch.job.entity.Job;
import com.backend.Skytouch.offer.apimodel.OfferResponse;
import com.backend.Skytouch.offer.entity.JobOffer;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
public class OfferMapper {

    public OfferResponse toResponse(JobOffer offer) {
        JobApplication application = offer.getApplication();
        Job job = application.getJob();
        return OfferResponse.builder()
                .id(offer.getId())
                .applicationId(application.getId())
                .jobTitle(job.getTitle())
                .companyName(job.getCompany().getName())
                .seekerName(application.getSeekerName())
                .salaryAmount(offer.getSalaryAmount())
                .salaryCurrency(offer.getSalaryCurrency())
                .startDate(offer.getStartDate())
                .terms(offer.getTerms())
                .status(offer.getStatus())
                .offeredAt(offer.getOfferedAt())
                .expiresAt(offer.getExpiresAt())
                .respondedAt(offer.getRespondedAt())
                .build();
    }

    public JobOffer toEntity(
            JobApplication application,
            com.backend.Skytouch.user.entity.Users offeredBy,
            com.backend.Skytouch.offer.apimodel.OfferCreateRequest request) {
        return JobOffer.builder()
                .application(application)
                .salaryAmount(request.getSalaryAmount())
                .salaryCurrency(StringUtils.hasText(request.getSalaryCurrency())
                        ? request.getSalaryCurrency().trim()
                        : "NGN")
                .startDate(request.getStartDate())
                .terms(request.getTerms())
                .status(com.backend.Skytouch.common.enums.OfferStatus.PENDING)
                .offeredAt(java.time.LocalDateTime.now())
                .expiresAt(request.getExpiresAt())
                .offeredBy(offeredBy)
                .build();
    }
}
