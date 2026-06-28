package com.backend.Skytouch.employer.service;

import com.backend.Skytouch.common.enums.UserRole;
import com.backend.Skytouch.common.exception.ResourceNotFoundException;
import com.backend.Skytouch.common.mapper.EmployerMapper;
import com.backend.Skytouch.common.profile.EmployerProfileCompletenessCalculator;
import com.backend.Skytouch.employer.apimodel.EmployerDashboardResponse;
import com.backend.Skytouch.employer.apimodel.EmployerDashboardStats;
import com.backend.Skytouch.employer.apimodel.EmployerProfileRequest;
import com.backend.Skytouch.employer.apimodel.EmployerResponse;
import com.backend.Skytouch.employer.entity.Employer;
import com.backend.Skytouch.employer.repository.EmployerRepository;
import com.backend.Skytouch.user.entity.Users;
import com.backend.Skytouch.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class EmployerService {

    private static final UserRole EMPLOYER_ROLE = UserRole.EMPLOYER;

    private final EmployerRepository employerRepository;
    private final UserRepository userRepository;
    private final EmployerMapper employerMapper;
    private final EmployerProfileCompletenessCalculator profileCompletenessCalculator;

    @Transactional(readOnly = true)
    public List<EmployerResponse> findAll() {
        return userRepository.findByRole(EMPLOYER_ROLE).stream()
                .map(this::toResponseWithProfile)
                .toList();
    }

    @Transactional(readOnly = true)
    public EmployerResponse findById(UUID id) {
        Users user = userRepository.findByIdAndRole(id, EMPLOYER_ROLE)
                .orElseThrow(() -> new ResourceNotFoundException("Employer not found: " + id));
        return toResponseWithProfile(user);
    }

    @Transactional(readOnly = true)
    public EmployerResponse findByEmail(String email) {
        Users user = userRepository.findByEmailAndRole(email, EMPLOYER_ROLE)
                .orElseThrow(() -> new ResourceNotFoundException("Employer not found: " + email));
        return toResponseWithProfile(user);
    }

    @Transactional(readOnly = true)
    public EmployerDashboardResponse getDashboard(String email) {
        Users user = userRepository.findByEmailAndRole(email, EMPLOYER_ROLE)
                .orElseThrow(() -> new ResourceNotFoundException("Employer not found: " + email));
        Employer profile = employerRepository.findByUser_Id(user.getId()).orElse(null);
        boolean companyLinked = false;

        return EmployerDashboardResponse.builder()
                .displayName(buildDisplayName(profile, user.getEmail()))
                .companyName(profile != null ? profile.getCompanyName() : null)
                .emailVerified(Boolean.TRUE.equals(user.getEmailVerified()))
                .companyLinked(companyLinked)
                .profileCompleteness(profileCompletenessCalculator.calculate(user, profile, companyLinked))
                .stats(EmployerDashboardStats.builder()
                        .activeJobsCount(0)
                        .totalApplicantsCount(0)
                        .draftJobsCount(0)
                        .build())
                .build();
    }

    @Transactional
    public EmployerResponse updateProfile(String email, EmployerProfileRequest request) {
        Employer profile = getProfileForUser(email);
        employerMapper.applyProfile(profile, request);
        return employerMapper.toResponse(profile.getUser(), employerRepository.save(profile));
    }

    private Employer getProfileForUser(String email) {
        Users user = userRepository.findByEmailAndRole(email, EMPLOYER_ROLE)
                .orElseThrow(() -> new ResourceNotFoundException("Employer not found: " + email));
        return employerRepository.findByUser_Id(user.getId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Employer profile not found: " + email));
    }

    private EmployerResponse toResponseWithProfile(Users user) {
        Employer profile = employerRepository.findByUser_Id(user.getId()).orElse(null);
        return employerMapper.toResponse(user, profile);
    }

    private String buildDisplayName(Employer profile, String email) {
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
