package com.backend.Skytouch.admin.service;

import com.backend.Skytouch.audit.service.AuditService;
import com.backend.Skytouch.common.enums.CompanyStatus;
import com.backend.Skytouch.common.enums.UserRole;
import com.backend.Skytouch.common.enums.UserStatus;
import com.backend.Skytouch.company.entity.Company;
import com.backend.Skytouch.company.repository.CompanyRepository;
import com.backend.Skytouch.employer.repository.EmployerRepository;
import com.backend.Skytouch.job.repository.JobRepository;
import com.backend.Skytouch.notification.repository.NotificationRepository;
import com.backend.Skytouch.user.entity.Users;
import com.backend.Skytouch.user.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AdminModerationServiceTest {

    @Mock
    private CompanyRepository companyRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private JobRepository jobRepository;

    @Mock
    private EmployerRepository employerRepository;

    @Mock
    private NotificationRepository notificationRepository;

    @Mock
    private AuditService auditService;

    @InjectMocks
    private AdminModerationService adminModerationService;

    @Test
    void approveCompany_setsActiveStatus() {
        UUID companyId = UUID.randomUUID();
        Company company = Company.builder().id(companyId).name("Acme Ltd").status(CompanyStatus.PENDING).build();

        when(companyRepository.findById(companyId)).thenReturn(Optional.of(company));
        when(companyRepository.save(company)).thenReturn(company);
        when(employerRepository.findByCompany_Id(companyId)).thenReturn(Optional.empty());

        UUID adminId = UUID.randomUUID();
        var response = adminModerationService.approveCompany(companyId, adminId);

        assertThat(response.getStatus()).isEqualTo(CompanyStatus.ACTIVE);
        assertThat(company.getStatus()).isEqualTo(CompanyStatus.ACTIVE);
        verify(auditService).record(eq(adminId), eq(com.backend.Skytouch.common.enums.AuditAction.COMPANY_APPROVED),
                eq(com.backend.Skytouch.common.enums.AuditTargetType.COMPANY), eq(companyId), anyString());
    }

    @Test
    void suspendUser_setsSuspendedStatus() {
        UUID userId = UUID.randomUUID();
        Users user = Users.builder()
                .id(userId)
                .email("seeker@example.com")
                .role(UserRole.JOB_SEEKER)
                .status(UserStatus.ACTIVE)
                .build();

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        adminModerationService.suspendUser(userId, UUID.randomUUID());

        assertThat(user.getStatus()).isEqualTo(UserStatus.SUSPENDED);
        verify(notificationRepository).save(any());
    }
}
