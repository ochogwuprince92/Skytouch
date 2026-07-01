package com.backend.Skytouch.offer.service;

import com.backend.Skytouch.application.entity.JobApplication;
import com.backend.Skytouch.application.repository.JobApplicationRepository;
import com.backend.Skytouch.common.apimodel.PageResponse;
import com.backend.Skytouch.common.enums.ApplicationStatus;
import com.backend.Skytouch.common.enums.InterviewStatus;
import com.backend.Skytouch.common.enums.JobStatus;
import com.backend.Skytouch.common.enums.OfferStatus;
import com.backend.Skytouch.common.enums.UserRole;
import com.backend.Skytouch.common.exception.BadRequestException;
import com.backend.Skytouch.common.exception.ConflictException;
import com.backend.Skytouch.common.exception.ResourceNotFoundException;
import com.backend.Skytouch.common.mapper.OfferMapper;
import com.backend.Skytouch.common.util.PaginationUtils;
import com.backend.Skytouch.company.entity.Company;
import com.backend.Skytouch.company.service.CompanyService;
import com.backend.Skytouch.interview.entity.Interview;
import com.backend.Skytouch.interview.repository.InterviewRepository;
import com.backend.Skytouch.job.entity.Job;
import com.backend.Skytouch.job.repository.JobRepository;
import com.backend.Skytouch.notification.service.NotificationService;
import com.backend.Skytouch.offer.apimodel.OfferCreateRequest;
import com.backend.Skytouch.offer.apimodel.OfferResponse;
import com.backend.Skytouch.offer.entity.JobOffer;
import com.backend.Skytouch.offer.repository.JobOfferRepository;
import com.backend.Skytouch.user.entity.Users;
import com.backend.Skytouch.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class OfferService {

    private final JobOfferRepository offerRepository;
    private final JobApplicationRepository applicationRepository;
    private final InterviewRepository interviewRepository;
    private final JobRepository jobRepository;
    private final OfferMapper offerMapper;
    private final CompanyService companyService;
    private final UserRepository userRepository;
    private final NotificationService notificationService;

    @Transactional
    public OfferResponse extendOffer(String employerEmail, UUID applicationId, OfferCreateRequest request) {
        JobApplication application = getEmployerApplication(employerEmail, applicationId);
        validateOfferEligibility(application);

        if (offerRepository.findByApplication_Id(applicationId).isPresent()) {
            throw new ConflictException("An offer already exists for this application");
        }

        Users employerUser = userRepository.findByEmailAndRole(employerEmail, UserRole.EMPLOYER)
                .orElseThrow(() -> new ResourceNotFoundException("Employer not found: " + employerEmail));

        JobOffer offer = offerMapper.toEntity(application, employerUser, request);
        application.setStatus(ApplicationStatus.OFFER_EXTENDED);
        applicationRepository.save(application);

        JobOffer saved = offerRepository.save(offer);
        notificationService.notifyOnOfferExtended(saved);
        return offerMapper.toResponse(saved);
    }

    @Transactional(readOnly = true)
    public OfferResponse findForApplication(String email, UUID applicationId) {
        getParticipantApplication(email, applicationId);
        JobOffer offer = offerRepository.findByApplication_Id(applicationId)
                .orElseThrow(() -> new ResourceNotFoundException("Offer not found for application: " + applicationId));
        return offerMapper.toResponse(offer);
    }

    @Transactional
    public OfferResponse acceptOffer(String seekerEmail, UUID offerId) {
        JobOffer offer = getSeekerOffer(seekerEmail, offerId);
        validatePendingOffer(offer);

        offer.setStatus(OfferStatus.ACCEPTED);
        offer.setRespondedAt(LocalDateTime.now());

        JobApplication application = offer.getApplication();
        application.setStatus(ApplicationStatus.HIRED);
        applicationRepository.save(application);

        closeJobIfActive(application.getJob());

        JobOffer saved = offerRepository.save(offer);
        notificationService.notifyOnOfferAccepted(saved);
        return offerMapper.toResponse(saved);
    }

    @Transactional
    public OfferResponse declineOffer(String seekerEmail, UUID offerId) {
        JobOffer offer = getSeekerOffer(seekerEmail, offerId);
        validatePendingOffer(offer);

        offer.setStatus(OfferStatus.DECLINED);
        offer.setRespondedAt(LocalDateTime.now());

        JobApplication application = offer.getApplication();
        application.setStatus(ApplicationStatus.OFFER_DECLINED);
        applicationRepository.save(application);

        JobOffer saved = offerRepository.save(offer);
        notificationService.notifyOnOfferDeclined(saved);
        return offerMapper.toResponse(saved);
    }

    @Transactional(readOnly = true)
    public PageResponse<OfferResponse> findMyPendingOffers(String seekerEmail, int page, int size) {
        Pageable pageable = PaginationUtils.pageable(page, size, Sort.by(Sort.Direction.DESC, "offeredAt"));
        Page<JobOffer> results = offerRepository.findBySeekerEmailAndStatus(
                seekerEmail, OfferStatus.PENDING, pageable);
        return PaginationUtils.mapPage(results, offerMapper::toResponse);
    }

    @Transactional(readOnly = true)
    public long countPendingForSeeker(String seekerEmail) {
        return offerRepository.countByApplication_JobSeeker_User_EmailAndStatus(
                seekerEmail, OfferStatus.PENDING);
    }

    @Transactional(readOnly = true)
    public long countOpenOffersForCompany(UUID companyId) {
        return offerRepository.countByCompanyIdAndStatus(companyId, OfferStatus.PENDING);
    }

    @Transactional(readOnly = true)
    public long countHiresForCompany(UUID companyId) {
        return offerRepository.countHiredByCompanyId(companyId, ApplicationStatus.HIRED);
    }

    private void validateOfferEligibility(JobApplication application) {
        if (application.getStatus() != ApplicationStatus.INTERVIEW_SCHEDULED) {
            throw new BadRequestException("Offers can only be extended after an interview is scheduled");
        }
        Interview interview = interviewRepository.findByApplication_Id(application.getId())
                .orElseThrow(() -> new BadRequestException("Complete the interview before extending an offer"));
        if (interview.getStatus() != InterviewStatus.COMPLETED) {
            throw new BadRequestException("Interview must be marked completed before extending an offer");
        }
    }

    private void validatePendingOffer(JobOffer offer) {
        if (offer.getStatus() != OfferStatus.PENDING) {
            throw new BadRequestException("This offer has already been responded to");
        }
        if (offer.getExpiresAt() != null && offer.getExpiresAt().isBefore(LocalDateTime.now())) {
            offer.setStatus(OfferStatus.EXPIRED);
            offerRepository.save(offer);
            throw new BadRequestException("This offer has expired");
        }
    }

    private void closeJobIfActive(Job job) {
        if (job.getStatus() == JobStatus.ACTIVE) {
            job.setStatus(JobStatus.CLOSED);
            job.setClosedAt(LocalDateTime.now());
            jobRepository.save(job);
        }
    }

    private JobOffer getSeekerOffer(String seekerEmail, UUID offerId) {
        return offerRepository.findByIdAndApplication_JobSeeker_User_Email(offerId, seekerEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Offer not found: " + offerId));
    }

    private JobApplication getEmployerApplication(String employerEmail, UUID applicationId) {
        JobApplication application = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new ResourceNotFoundException("Application not found: " + applicationId));
        Company company = companyService.getLinkedCompany(employerEmail);
        if (!application.getJob().getCompany().getId().equals(company.getId())) {
            throw new ResourceNotFoundException("Application not found: " + applicationId);
        }
        return application;
    }

    private JobApplication getParticipantApplication(String email, UUID applicationId) {
        JobApplication application = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new ResourceNotFoundException("Application not found: " + applicationId));

        if (application.getJobSeeker().getUser().getEmail().equals(email)) {
            return application;
        }

        try {
            Company company = companyService.getLinkedCompany(email);
            if (application.getJob().getCompany().getId().equals(company.getId())) {
                return application;
            }
        } catch (BadRequestException | ResourceNotFoundException ignored) {
            // not employer on this application
        }

        throw new ResourceNotFoundException("Application not found: " + applicationId);
    }
}
