package com.backend.Skytouch.admin.service;

import com.backend.Skytouch.admin.apimodel.CompanyModerationResponse;
import com.backend.Skytouch.admin.apimodel.JobModerationResponse;
import com.backend.Skytouch.admin.apimodel.UserModerationResponse;
import com.backend.Skytouch.audit.service.AuditService;
import com.backend.Skytouch.common.apimodel.PageResponse;
import com.backend.Skytouch.common.enums.AuditAction;
import com.backend.Skytouch.common.enums.AuditTargetType;
import com.backend.Skytouch.common.enums.CompanyStatus;
import com.backend.Skytouch.common.enums.JobStatus;
import com.backend.Skytouch.common.enums.NotificationType;
import com.backend.Skytouch.common.enums.UserRole;
import com.backend.Skytouch.common.enums.UserStatus;
import com.backend.Skytouch.common.exception.ResourceNotFoundException;
import com.backend.Skytouch.common.util.PaginationUtils;
import com.backend.Skytouch.company.entity.Company;
import com.backend.Skytouch.company.repository.CompanyRepository;
import com.backend.Skytouch.employer.repository.EmployerRepository;
import com.backend.Skytouch.job.entity.Job;
import com.backend.Skytouch.job.repository.JobRepository;
import com.backend.Skytouch.notification.entity.Notification;
import com.backend.Skytouch.notification.repository.NotificationRepository;
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
public class AdminModerationService {

    private final CompanyRepository companyRepository;
    private final UserRepository userRepository;
    private final JobRepository jobRepository;
    private final EmployerRepository employerRepository;
    private final NotificationRepository notificationRepository;
    private final AuditService auditService;

    @Transactional(readOnly = true)
    public PageResponse<CompanyModerationResponse> findPendingCompanies(int page, int size) {
        Pageable pageable = PaginationUtils.pageable(page, size, Sort.by(Sort.Direction.ASC, "createdAt"));
        Page<Company> results = companyRepository.findByStatus(CompanyStatus.PENDING, pageable);
        return PaginationUtils.mapPage(results, this::toModerationResponse);
    }

    @Transactional(readOnly = true)
    public PageResponse<CompanyModerationResponse> listCompanies(int page, int size, String status) {
        Pageable pageable = PaginationUtils.pageable(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<Company> results;
        if (status != null && !status.isBlank()) {
            try {
                CompanyStatus statusEnum = CompanyStatus.valueOf(status.toUpperCase());
                results = companyRepository.findByStatus(statusEnum, pageable);
            } catch (IllegalArgumentException e) {
                results = companyRepository.findAll(pageable);
            }
        } else {
            results = companyRepository.findAll(pageable);
        }
        return PaginationUtils.mapPage(results, this::toModerationResponse);
    }

    @Transactional(readOnly = true)
    public PageResponse<UserModerationResponse> listUsers(int page, int size, String email, String status, Boolean emailVerified) {
        Pageable pageable = PaginationUtils.pageable(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<Users> results;
        
        if (email != null && !email.isBlank()) {
            results = userRepository.findByEmailContaining(email, pageable);
        } else if (status != null && !status.isBlank()) {
            try {
                UserStatus statusEnum = UserStatus.valueOf(status.toUpperCase());
                results = userRepository.findByStatus(statusEnum, pageable);
            } catch (IllegalArgumentException e) {
                results = userRepository.findAll(pageable);
            }
        } else if (emailVerified != null && emailVerified == false) {
            results = userRepository.findByEmailVerifiedFalse(pageable);
        } else {
            results = userRepository.findAll(pageable);
        }
        return PaginationUtils.mapPage(results, this::toUserResponse);
    }

    @Transactional
    public CompanyModerationResponse approveCompany(UUID companyId, UUID adminUserId) {
        Company company = getCompany(companyId);
        if (company.getStatus() != CompanyStatus.PENDING) {
            throw new ResourceNotFoundException("Company is not pending approval: " + companyId);
        }
        company.setStatus(CompanyStatus.ACTIVE);
        Company saved = companyRepository.save(company);
        notifyEmployerForCompany(saved, NotificationType.COMPANY_APPROVED,
                "Company approved",
                "Your company \"" + saved.getName() + "\" has been approved. You can now publish jobs.");
        auditService.record(adminUserId, AuditAction.COMPANY_APPROVED, AuditTargetType.COMPANY,
                saved.getId(), "Approved company: " + saved.getName());
        return toModerationResponse(saved);
    }

    @Transactional
    public CompanyModerationResponse rejectCompany(UUID companyId, UUID adminUserId) {
        Company company = getCompany(companyId);
        if (company.getStatus() != CompanyStatus.PENDING) {
            throw new ResourceNotFoundException("Company is not pending approval: " + companyId);
        }
        company.setStatus(CompanyStatus.REJECTED);
        Company saved = companyRepository.save(company);
        notifyEmployerForCompany(saved, NotificationType.COMPANY_REJECTED,
                "Company rejected",
                "Your company \"" + saved.getName() + "\" was not approved. Contact support for details.");
        auditService.record(adminUserId, AuditAction.COMPANY_REJECTED, AuditTargetType.COMPANY,
                saved.getId(), "Rejected company: " + saved.getName());
        return toModerationResponse(saved);
    }

    @Transactional
    public void suspendUser(UUID userId, UUID adminUserId) {
        Users user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userId));
        if (user.getRole() == UserRole.ADMIN) {
            throw new ResourceNotFoundException("Admin accounts cannot be suspended via this endpoint");
        }
        user.setStatus(UserStatus.SUSPENDED);
        userRepository.save(user);

        Notification notification = Notification.builder()
                .user(user)
                .type(NotificationType.ACCOUNT_SUSPENDED)
                .title("Account suspended")
                .message("Your Skytouch account has been suspended. Contact support if you believe this is an error.")
                .read(false)
                .build();
        notificationRepository.save(notification);
        auditService.record(adminUserId, AuditAction.USER_SUSPENDED, AuditTargetType.USER,
                userId, "Suspended user: " + user.getEmail());
    }

    @Transactional
    public void activateUser(UUID userId, UUID adminUserId) {
        Users user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userId));
        if (user.getRole() == UserRole.ADMIN) {
            throw new ResourceNotFoundException("Admin accounts cannot be modified via this endpoint");
        }
        user.setStatus(UserStatus.ACTIVE);
        userRepository.save(user);

        Notification notification = Notification.builder()
                .user(user)
                .type(NotificationType.ACCOUNT_ACTIVATED)
                .title("Account reactivated")
                .message("Your Skytouch account has been reactivated. You can now access your account.")
                .read(false)
                .build();
        notificationRepository.save(notification);
        auditService.record(adminUserId, AuditAction.USER_ACTIVATED, AuditTargetType.USER,
                userId, "Activated user: " + user.getEmail());
    }

    @Transactional
    public void suspendCompany(UUID companyId, UUID adminUserId) {
        Company company = getCompany(companyId);
        if (company.getStatus() != CompanyStatus.ACTIVE) {
            throw new ResourceNotFoundException("Only active companies can be suspended: " + companyId);
        }
        company.setStatus(CompanyStatus.SUSPENDED);
        Company saved = companyRepository.save(company);
        notifyEmployerForCompany(saved, NotificationType.COMPANY_SUSPENDED,
                "Company suspended",
                "Your company \"" + saved.getName() + "\" has been suspended. Contact support for details.");
        auditService.record(adminUserId, AuditAction.COMPANY_SUSPENDED, AuditTargetType.COMPANY,
                saved.getId(), "Suspended company: " + saved.getName());
    }

    @Transactional
    public void activateCompany(UUID companyId, UUID adminUserId) {
        Company company = getCompany(companyId);
        if (company.getStatus() != CompanyStatus.SUSPENDED) {
            throw new ResourceNotFoundException("Only suspended companies can be activated: " + companyId);
        }
        company.setStatus(CompanyStatus.ACTIVE);
        Company saved = companyRepository.save(company);
        notifyEmployerForCompany(saved, NotificationType.COMPANY_ACTIVATED,
                "Company reactivated",
                "Your company \"" + saved.getName() + "\" has been reactivated. You can now publish jobs.");
        auditService.record(adminUserId, AuditAction.COMPANY_ACTIVATED, AuditTargetType.COMPANY,
                saved.getId(), "Activated company: " + saved.getName());
    }

    @Transactional
    public void forceCloseJob(UUID jobId, UUID adminUserId) {
        Job job = jobRepository.findById(jobId)
                .orElseThrow(() -> new ResourceNotFoundException("Job not found: " + jobId));
        if (job.getStatus() == JobStatus.CLOSED) {
            return;
        }
        job.setStatus(JobStatus.CLOSED);
        job.setClosedAt(LocalDateTime.now());
        jobRepository.save(job);
        auditService.record(adminUserId, AuditAction.JOB_FORCE_CLOSED, AuditTargetType.JOB,
                jobId, "Force-closed job: " + job.getTitle());
    }

    @Transactional(readOnly = true)
    public long countPendingCompanies() {
        return companyRepository.countByStatus(CompanyStatus.PENDING);
    }

    @Transactional(readOnly = true)
    public PageResponse<JobModerationResponse> listJobs(int page, int size, String status) {
        Pageable pageable = PaginationUtils.pageable(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<Job> results;
        if (status != null && !status.isBlank()) {
            try {
                JobStatus statusEnum = JobStatus.valueOf(status.toUpperCase());
                results = jobRepository.findByStatus(statusEnum, pageable);
            } catch (IllegalArgumentException e) {
                results = jobRepository.findAll(pageable);
            }
        } else {
            results = jobRepository.findAll(pageable);
        }
        return PaginationUtils.mapPage(results, this::toJobModerationResponse);
    }

    private Company getCompany(UUID companyId) {
        return companyRepository.findById(companyId)
                .orElseThrow(() -> new ResourceNotFoundException("Company not found: " + companyId));
    }

    private void notifyEmployerForCompany(Company company, NotificationType type, String title, String message) {
        employerRepository.findByCompany_Id(company.getId()).ifPresent(employer -> {
            Notification notification = Notification.builder()
                    .user(employer.getUser())
                    .type(type)
                    .title(title)
                    .message(message)
                    .read(false)
                    .build();
            notificationRepository.save(notification);
        });
    }

    private CompanyModerationResponse toModerationResponse(Company company) {
        return CompanyModerationResponse.builder()
                .id(company.getId())
                .name(company.getName())
                .industry(company.getIndustry() != null ? company.getIndustry().name() : null)
                .status(company.getStatus())
                .build();
    }

    private UserModerationResponse toUserResponse(Users user) {
        return UserModerationResponse.builder()
                .id(user.getId().toString())
                .email(user.getEmail())
                .role(user.getRole().name())
                .status(user.getStatus().name())
                .emailVerified(user.getEmailVerified())
                .createdAt(user.getCreatedAt().toString())
                .build();
    }

    private JobModerationResponse toJobModerationResponse(Job job) {
        return JobModerationResponse.builder()
                .id(job.getId())
                .companyName(job.getCompany() != null ? job.getCompany().getName() : "")
                .title(job.getTitle())
                .employmentType(job.getEmploymentType())
                .workMode(job.getWorkMode())
                .salaryMin(job.getSalaryMin())
                .salaryMax(job.getSalaryMax())
                .locationState(job.getLocationState())
                .status(job.getStatus())
                .createdAt(job.getCreatedAt())
                .build();
    }
}
