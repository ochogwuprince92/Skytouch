package com.backend.Skytouch.employer.service;

import com.backend.Skytouch.common.enums.UserRole;
import com.backend.Skytouch.common.enums.UserStatus;
import com.backend.Skytouch.common.mapper.EmployerMapper;
import com.backend.Skytouch.common.profile.EmployerProfileCompletenessCalculator;
import com.backend.Skytouch.common.profile.ProfileCompleteness;
import com.backend.Skytouch.common.profile.ProfileStep;
import com.backend.Skytouch.company.entity.Company;
import com.backend.Skytouch.common.enums.CompanyStatus;
import com.backend.Skytouch.common.enums.JobStatus;
import com.backend.Skytouch.employer.apimodel.EmployerProfileRequest;
import com.backend.Skytouch.employer.apimodel.EmployerResponse;
import com.backend.Skytouch.employer.entity.Employer;
import com.backend.Skytouch.employer.repository.EmployerRepository;
import com.backend.Skytouch.job.repository.JobRepository;
import com.backend.Skytouch.user.entity.Users;
import com.backend.Skytouch.user.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EmployerServiceTest {

    @Mock
    private EmployerRepository employerRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private EmployerMapper employerMapper;

    @Mock
    private EmployerProfileCompletenessCalculator profileCompletenessCalculator;

    @Mock
    private JobRepository jobRepository;

    @InjectMocks
    private EmployerService employerService;

    @Test
    void updateProfile_appliesFields() {
        UUID userId = UUID.randomUUID();
        Users user = Users.builder()
                .id(userId)
                .email("employer@example.com")
                .role(UserRole.EMPLOYER)
                .status(UserStatus.ACTIVE)
                .build();
        Employer profile = Employer.builder()
                .id(UUID.randomUUID())
                .user(user)
                .status(UserStatus.ACTIVE)
                .phone("+2348012345678")
                .build();

        EmployerProfileRequest request = new EmployerProfileRequest();
        request.setCompanyName("Acme Ltd");
        request.setJobTitle("HR Manager");

        when(userRepository.findByEmailAndRole("employer@example.com", UserRole.EMPLOYER))
                .thenReturn(Optional.of(user));
        when(employerRepository.findByUser_Id(userId)).thenReturn(Optional.of(profile));
        when(employerRepository.save(profile)).thenReturn(profile);
        when(employerMapper.toResponse(user, profile)).thenReturn(
                EmployerResponse.builder()
                        .email(user.getEmail())
                        .companyName("Acme Ltd")
                        .jobTitle("HR Manager")
                        .build());

        employerService.updateProfile("employer@example.com", request);

        verify(employerMapper).applyProfile(eq(profile), eq(request));
        verify(employerRepository).save(profile);
    }

    @Test
    void getDashboard_returnsCompanyLinkedFalseAndPlaceholderStats() {
        UUID userId = UUID.randomUUID();
        Users user = Users.builder()
                .id(userId)
                .email("employer@example.com")
                .role(UserRole.EMPLOYER)
                .status(UserStatus.ACTIVE)
                .emailVerified(true)
                .build();
        Employer profile = Employer.builder()
                .user(user)
                .status(UserStatus.ACTIVE)
                .firstName("Jane")
                .lastName("Doe")
                .companyName("Acme Ltd")
                .phone("+2348012345678")
                .build();
        ProfileCompleteness completeness = ProfileCompleteness.builder()
                .percentComplete(80)
                .steps(List.of(ProfileStep.builder()
                        .key("company_linked")
                        .label("Link company")
                        .complete(false)
                        .build()))
                .build();

        when(userRepository.findByEmailAndRole("employer@example.com", UserRole.EMPLOYER))
                .thenReturn(Optional.of(user));
        when(employerRepository.findByUser_Id(userId)).thenReturn(Optional.of(profile));
        when(profileCompletenessCalculator.calculate(user, profile, false)).thenReturn(completeness);

        var result = employerService.getDashboard("employer@example.com");

        assertThat(result.getDisplayName()).isEqualTo("Jane Doe");
        assertThat(result.getCompanyName()).isEqualTo("Acme Ltd");
        assertThat(result.isCompanyLinked()).isFalse();
        assertThat(result.getStats().getActiveJobsCount()).isZero();
    }

    @Test
    void getDashboard_returnsCompanyLinkedTrueWhenCompanyExists() {
        UUID userId = UUID.randomUUID();
        Users user = Users.builder()
                .id(userId)
                .email("employer@example.com")
                .role(UserRole.EMPLOYER)
                .status(UserStatus.ACTIVE)
                .emailVerified(true)
                .build();
        Company company = Company.builder()
                .id(UUID.randomUUID())
                .name("Acme Ltd")
                .status(CompanyStatus.ACTIVE)
                .build();
        Employer profile = Employer.builder()
                .user(user)
                .status(UserStatus.ACTIVE)
                .company(company)
                .companyName("Old Name")
                .phone("+2348012345678")
                .build();
        ProfileCompleteness completeness = ProfileCompleteness.builder()
                .percentComplete(100)
                .steps(List.of())
                .build();

        when(userRepository.findByEmailAndRole("employer@example.com", UserRole.EMPLOYER))
                .thenReturn(Optional.of(user));
        when(employerRepository.findByUser_Id(userId)).thenReturn(Optional.of(profile));
        when(profileCompletenessCalculator.calculate(user, profile, true)).thenReturn(completeness);

        var result = employerService.getDashboard("employer@example.com");

        assertThat(result.isCompanyLinked()).isTrue();
        assertThat(result.getCompanyName()).isEqualTo("Acme Ltd");
        verify(profileCompletenessCalculator).calculate(user, profile, true);
    }

    @Test
    void getDashboard_returnsJobCountsWhenCompanyLinked() {
        UUID userId = UUID.randomUUID();
        UUID companyId = UUID.randomUUID();
        Users user = Users.builder()
                .id(userId)
                .email("employer@example.com")
                .role(UserRole.EMPLOYER)
                .status(UserStatus.ACTIVE)
                .emailVerified(true)
                .build();
        Company company = Company.builder()
                .id(companyId)
                .name("Acme Ltd")
                .status(CompanyStatus.ACTIVE)
                .build();
        Employer profile = Employer.builder()
                .user(user)
                .status(UserStatus.ACTIVE)
                .company(company)
                .phone("+2348012345678")
                .build();
        ProfileCompleteness completeness = ProfileCompleteness.builder()
                .percentComplete(100)
                .steps(List.of())
                .build();

        when(userRepository.findByEmailAndRole("employer@example.com", UserRole.EMPLOYER))
                .thenReturn(Optional.of(user));
        when(employerRepository.findByUser_Id(userId)).thenReturn(Optional.of(profile));
        when(jobRepository.countByCompany_IdAndStatus(companyId, JobStatus.ACTIVE)).thenReturn(2L);
        when(jobRepository.countByCompany_IdAndStatus(companyId, JobStatus.DRAFT)).thenReturn(1L);
        when(profileCompletenessCalculator.calculate(user, profile, true)).thenReturn(completeness);

        var result = employerService.getDashboard("employer@example.com");

        assertThat(result.getStats().getActiveJobsCount()).isEqualTo(2);
        assertThat(result.getStats().getDraftJobsCount()).isEqualTo(1);
    }
}
