package com.backend.Skytouch.admin.export;

import com.backend.Skytouch.application.entity.JobApplication;
import com.backend.Skytouch.application.repository.JobApplicationRepository;
import com.backend.Skytouch.common.enums.ApplicationStatus;
import com.backend.Skytouch.common.enums.CompanyStatus;
import com.backend.Skytouch.common.enums.JobStatus;
import com.backend.Skytouch.common.enums.UserRole;
import com.backend.Skytouch.common.enums.UserStatus;
import com.backend.Skytouch.common.exception.BadRequestException;
import com.backend.Skytouch.company.entity.Company;
import com.backend.Skytouch.company.repository.CompanyRepository;
import com.backend.Skytouch.job.entity.Job;
import com.backend.Skytouch.job.repository.JobRepository;
import com.backend.Skytouch.jobseeker.entity.JobSeeker;
import com.backend.Skytouch.user.entity.Users;
import com.backend.Skytouch.user.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AdminExportServiceTest {

    @Mock private UserRepository userRepository;
    @Mock private JobRepository jobRepository;
    @Mock private JobApplicationRepository applicationRepository;
    @Mock private CompanyRepository companyRepository;

    @InjectMocks
    private AdminExportService adminExportService;

    @Test
    void exportUsers_returnsCsvHeader() {
        Users user = Users.builder()
                .id(UUID.randomUUID())
                .email("user@example.com")
                .role(UserRole.JOB_SEEKER)
                .status(UserStatus.ACTIVE)
                .emailVerified(true)
                .active(true)
                .createdAt(LocalDateTime.now())
                .build();
        Page<Users> page = new PageImpl<>(List.of(user));
        when(userRepository.findAll(any(Pageable.class))).thenReturn(page);

        String csv = new String(adminExportService.export(AdminExportType.USERS), StandardCharsets.UTF_8);

        assertThat(csv).startsWith("user_id,email,role,status");
        assertThat(csv).contains("user@example.com");
    }

    @Test
    void exportApplications_includesJobAndCompany() {
        Company company = Company.builder().id(UUID.randomUUID()).name("Acme").build();
        Job job = Job.builder().id(UUID.randomUUID()).title("Engineer").company(company).build();
        Users seekerUser = Users.builder().email("seeker@example.com").role(UserRole.JOB_SEEKER).build();
        JobSeeker seeker = JobSeeker.builder().user(seekerUser).build();
        JobApplication application = JobApplication.builder()
                .id(UUID.randomUUID())
                .job(job)
                .jobSeeker(seeker)
                .seekerName("Ada")
                .status(ApplicationStatus.SUBMITTED)
                .appliedAt(LocalDateTime.now())
                .build();

        when(applicationRepository.findAllForPlatformExport(any(Pageable.class)))
                .thenReturn(List.of(application));

        String csv = new String(adminExportService.export(AdminExportType.APPLICATIONS), StandardCharsets.UTF_8);

        assertThat(csv).contains("Engineer");
        assertThat(csv).contains("Acme");
        assertThat(csv).contains("seeker@example.com");
    }

    @Test
    void parseType_rejectsInvalidValue() {
        assertThatThrownBy(() -> AdminExportService.parseType("invalid"))
                .isInstanceOf(BadRequestException.class);
    }
}
