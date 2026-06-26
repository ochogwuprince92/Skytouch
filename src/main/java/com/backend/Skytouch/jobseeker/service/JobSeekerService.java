package com.backend.Skytouch.jobseeker.service;

import com.backend.Skytouch.common.enums.UserRole;
import com.backend.Skytouch.common.exception.BadRequestException;
import com.backend.Skytouch.common.exception.ResourceNotFoundException;
import com.backend.Skytouch.common.mapper.JobSeekerMapper;
import com.backend.Skytouch.jobseeker.apimodel.JobSeekerKycRequest;
import com.backend.Skytouch.jobseeker.apimodel.JobSeekerOnboardingRequest;
import com.backend.Skytouch.jobseeker.apimodel.JobSeekerResponse;
import com.backend.Skytouch.jobseeker.entity.JobSeeker;
import com.backend.Skytouch.jobseeker.repository.JobSeekerRepository;
import com.backend.Skytouch.user.entity.Users;
import com.backend.Skytouch.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class JobSeekerService {

    private static final UserRole JOB_SEEKER_ROLE = UserRole.JOB_SEEKER;

    private final JobSeekerRepository jobSeekerRepository;
    private final UserRepository userRepository;
    private final JobSeekerMapper jobSeekerMapper;
    private final FileStorageService fileStorageService;

    @Transactional(readOnly = true)
    public List<JobSeekerResponse> findAll() {
        return userRepository.findByRole(JOB_SEEKER_ROLE).stream()
                .map(this::toResponseWithProfile)
                .toList();
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
        return jobSeekerMapper.toResponse(profile.getUser(), jobSeekerRepository.save(profile));
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
}