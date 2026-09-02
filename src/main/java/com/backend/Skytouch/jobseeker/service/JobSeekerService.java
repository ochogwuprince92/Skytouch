package com.backend.Skytouch.jobseeker.service;

import com.backend.Skytouch.application.service.ApplicationService;
import com.backend.Skytouch.common.address.AddressValidationService;
import com.backend.Skytouch.common.address.ValidatedAddress;
import com.backend.Skytouch.interview.service.InterviewService;
import com.backend.Skytouch.jobalert.service.JobAlertService;
import com.backend.Skytouch.offer.service.OfferService;
import com.backend.Skytouch.savedjob.service.SavedJobService;
import com.backend.Skytouch.common.apimodel.PageResponse;
import com.backend.Skytouch.common.enums.UserRole;
import com.backend.Skytouch.common.exception.BadRequestException;
import com.backend.Skytouch.common.exception.ResourceNotFoundException;
import com.backend.Skytouch.common.mapper.JobSeekerMapper;
import com.backend.Skytouch.common.profile.JobSeekerProfileCompletenessCalculator;
import com.backend.Skytouch.common.util.PaginationUtils;
import com.backend.Skytouch.jobseeker.apimodel.JobSeekerDashboardResponse;
import com.backend.Skytouch.jobseeker.apimodel.JobSeekerDashboardStats;
import com.backend.Skytouch.jobseeker.apimodel.JobSeekerKycRequest;
import com.backend.Skytouch.jobseeker.apimodel.JobSeekerOnboardingRequest;
import com.backend.Skytouch.jobseeker.apimodel.JobSeekerResponse;
import com.backend.Skytouch.jobseeker.entity.JobSeeker;
import com.backend.Skytouch.jobseeker.repository.JobSeekerRepository;
import com.backend.Skytouch.user.entity.Users;
import com.backend.Skytouch.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class JobSeekerService {

    private static final UserRole JOB_SEEKER_ROLE = UserRole.JOB_SEEKER;

    private final JobSeekerRepository jobSeekerRepository;
    private final UserRepository userRepository;
    private final JobSeekerMapper jobSeekerMapper;
    private final FileStorageService fileStorageService;
    private final AddressValidationService addressValidationService;
    private final JobSeekerProfileCompletenessCalculator profileCompletenessCalculator;
    private final ApplicationService applicationService;
    private final SavedJobService savedJobService;
    private final InterviewService interviewService;
    private final OfferService offerService;
    private final JobAlertService jobAlertService;

    @Transactional(readOnly = true)
    public PageResponse<JobSeekerResponse> findAll(int page, int size) {
        Page<Users> users = userRepository.findByRole(
                JOB_SEEKER_ROLE,
                PaginationUtils.pageable(page, size, Sort.by(Sort.Direction.ASC, "email")));
        return PaginationUtils.mapPage(users, this::toResponseWithProfile);
    }

    @Transactional(readOnly = true)
    public JobSeekerResponse findById(UUID id) {
        Users user = userRepository.findByIdAndRole(id, JOB_SEEKER_ROLE)
                .orElseThrow(() -> new ResourceNotFoundException("Job seeker not found: " + id));
        return toResponseWithProfile(user);
    }

    @Transactional(readOnly = true)
    public JobSeekerResponse findByEmail(String email) {
        Users user = userRepository.findByEmailAndRole(email, JOB_SEEKER_ROLE)
                .orElseThrow(() -> new ResourceNotFoundException("Job seeker not found: " + email));
        return toResponseWithProfile(user);
    }

    @Transactional(readOnly = true)
    public JobSeekerDashboardResponse getDashboard(String email) {
        Users user = userRepository.findByEmailAndRole(email, JOB_SEEKER_ROLE)
                .orElseThrow(() -> new ResourceNotFoundException("Job seeker not found: " + email));
        JobSeeker profile = jobSeekerRepository.findByUser_Id(user.getId()).orElse(null);

        return JobSeekerDashboardResponse.builder()
                .displayName(buildDisplayName(profile, user.getEmail()))
                .emailVerified(Boolean.TRUE.equals(user.getEmailVerified()))
                .openToWork(profile != null ? profile.getOpenToWork() : false)
                .profileCompleteness(profileCompletenessCalculator.calculate(user, profile))
                .stats(JobSeekerDashboardStats.builder()
                        .applicationsCount(applicationService.countApplicationsForSeeker(email))
                        .savedJobsCount(savedJobService.countForSeeker(email))
                        .interviewsCount(interviewService.countUpcomingForSeeker(email))
                        .pendingOffersCount(offerService.countPendingForSeeker(email))
                        .jobAlertsCount(jobAlertService.countActiveForSeeker(email))
                        .build())
                .build();
    }

    @Transactional
    public JobSeekerResponse updateOnboarding(String email, JobSeekerOnboardingRequest request) {
        JobSeeker profile = getProfileForUser(email);

        String cvUrl = null;
        if (request.getCv() != null && !request.getCv().isEmpty()) {
            if (!"application/pdf".equalsIgnoreCase(request.getCv().getContentType())) {
                throw new BadRequestException("Only PDF files are allowed");
            }
            cvUrl = fileStorageService.uploadPdf(request.getCv());
        }

        jobSeekerMapper.applyOnboarding(profile, request, cvUrl);
        return jobSeekerMapper.toResponse(profile.getUser(), jobSeekerRepository.save(profile));
    }

    @Transactional
    public JobSeekerResponse updateKyc(String email, JobSeekerKycRequest request) {
        JobSeeker profile = getProfileForUser(email);
        jobSeekerMapper.applyKyc(profile, request);
        if (StringUtils.hasText(request.getAddress())) {
            applyValidatedAddress(profile, request.getAddress());
        } else if (StringUtils.hasText(request.getAddressLine())) {
            applyValidatedAddress(profile, request.getAddressLine());
        }
        return jobSeekerMapper.toResponse(profile.getUser(), jobSeekerRepository.save(profile));
    }

    @Transactional
    public String uploadCv(String email, org.springframework.web.multipart.MultipartFile cv) {
        if (cv == null || cv.isEmpty()) {
            throw new BadRequestException("CV file is required");
        }
        if (!"application/pdf".equalsIgnoreCase(cv.getContentType())) {
            throw new BadRequestException("Only PDF files are allowed");
        }
        return fileStorageService.uploadPdf(cv);
    }

    private JobSeeker getProfileForUser(String email) {
        Users user = userRepository.findByEmailAndRole(email, JOB_SEEKER_ROLE)
                .orElseThrow(() -> new ResourceNotFoundException("Job seeker not found: " + email));
        return jobSeekerRepository.findByUser_Id(user.getId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Job seeker profile not found: " + email));
    }

    private JobSeekerResponse toResponseWithProfile(Users user) {
        JobSeeker profile = jobSeekerRepository.findByUser_Id(user.getId()).orElse(null);
        return jobSeekerMapper.toResponse(user, profile);
    }

    private void applyValidatedAddress(JobSeeker profile, String address) {
        ValidatedAddress validated = addressValidationService.validate(address);
        if (validated == null) {
            return;
        }
        profile.setAddressLine(validated.addressLine());
        if (StringUtils.hasText(validated.lga())) {
            profile.setAddressLga(validated.lga());
        }
        if (StringUtils.hasText(validated.state())) {
            profile.setAddressState(validated.state());
        }
    }

    private String buildDisplayName(JobSeeker profile, String email) {
        if (profile == null) {
            return email;
        }
        String first = profile.getFirstName();
        String last = profile.getLastName();
        if (StringUtils.hasText(first) && StringUtils.hasText(last)) {
            return first.trim() + " " + last.trim();
        }
        if (StringUtils.hasText(first)) {
            return first.trim();
        }
        return email;
    }
}