package com.backend.Skytouch.notification.service;

import com.backend.Skytouch.application.entity.JobApplication;
import com.backend.Skytouch.authentication.service.EmailService;
import com.backend.Skytouch.common.enums.ApplicationStatus;
import com.backend.Skytouch.common.enums.NotificationType;
import com.backend.Skytouch.common.enums.UserRole;
import com.backend.Skytouch.common.enums.UserStatus;
import com.backend.Skytouch.common.exception.ResourceNotFoundException;
import com.backend.Skytouch.common.mapper.NotificationMapper;
import com.backend.Skytouch.company.entity.Company;
import com.backend.Skytouch.employer.entity.Employer;
import com.backend.Skytouch.employer.repository.EmployerRepository;
import com.backend.Skytouch.job.entity.Job;
import com.backend.Skytouch.jobseeker.entity.JobSeeker;
import com.backend.Skytouch.notification.apimodel.NotificationResponse;
import com.backend.Skytouch.notification.entity.Notification;
import com.backend.Skytouch.notification.repository.NotificationRepository;
import com.backend.Skytouch.user.entity.Users;
import com.backend.Skytouch.user.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {

    @Mock
    private NotificationRepository notificationRepository;

    @Mock
    private NotificationMapper notificationMapper;

    @Mock
    private UserRepository userRepository;

    @Mock
    private EmployerRepository employerRepository;

    @Mock
    private EmailService emailService;

    @InjectMocks
    private NotificationService notificationService;

    @Test
    void notifyOnApplicationSubmitted_createsSeekerAndEmployerNotifications() {
        UUID applicationId = UUID.randomUUID();
        UUID companyId = UUID.randomUUID();
        Company company = Company.builder().id(companyId).name("Acme Ltd").build();
        Job job = Job.builder().title("Backend Engineer").company(company).build();
        Users seekerUser = user(UUID.randomUUID(), "seeker@example.com", UserRole.JOB_SEEKER);
        Users employerUser = user(UUID.randomUUID(), "employer@example.com", UserRole.EMPLOYER);
        JobSeeker jobSeeker = JobSeeker.builder().user(seekerUser).build();
        JobApplication application = JobApplication.builder()
                .id(applicationId)
                .job(job)
                .jobSeeker(jobSeeker)
                .seekerName("Ada Okafor")
                .status(ApplicationStatus.SUBMITTED)
                .build();
        Employer employer = Employer.builder().user(employerUser).company(company).build();

        when(notificationRepository.save(any(Notification.class))).thenAnswer(invocation -> {
            Notification notification = invocation.getArgument(0);
            notification.setId(UUID.randomUUID());
            return notification;
        });
        when(employerRepository.findByCompany_Id(companyId)).thenReturn(Optional.of(employer));

        notificationService.notifyOnApplicationSubmitted(application);

        ArgumentCaptor<Notification> captor = ArgumentCaptor.forClass(Notification.class);
        verify(notificationRepository, times(2)).save(captor.capture());
        assertThat(captor.getAllValues())
                .extracting(Notification::getType)
                .containsExactly(
                        NotificationType.APPLICATION_SUBMITTED,
                        NotificationType.NEW_APPLICATION);
        verify(emailService).sendApplicationSubmittedConfirmation(
                "seeker@example.com", "Backend Engineer", "Acme Ltd");
        verify(emailService).sendNewApplicationAlert(
                "employer@example.com", "Ada Okafor", "Backend Engineer");
    }

    @Test
    void notifyOnStatusUpdated_notifiesSeeker() {
        UUID applicationId = UUID.randomUUID();
        Users seekerUser = user(UUID.randomUUID(), "seeker@example.com", UserRole.JOB_SEEKER);
        Job job = Job.builder().title("Backend Engineer").build();
        JobSeeker jobSeeker = JobSeeker.builder().user(seekerUser).build();
        JobApplication application = JobApplication.builder()
                .id(applicationId)
                .job(job)
                .jobSeeker(jobSeeker)
                .status(ApplicationStatus.SHORTLISTED)
                .build();

        when(notificationRepository.save(any(Notification.class))).thenAnswer(invocation -> invocation.getArgument(0));

        notificationService.notifyOnStatusUpdated(application);

        ArgumentCaptor<Notification> captor = ArgumentCaptor.forClass(Notification.class);
        verify(notificationRepository).save(captor.capture());
        assertThat(captor.getValue().getType()).isEqualTo(NotificationType.APPLICATION_STATUS_UPDATED);
        verify(emailService).sendApplicationStatusUpdate(
                "seeker@example.com", "Backend Engineer", "shortlisted");
    }

    @Test
    void findMyNotifications_returnsPaginatedResults() {
        UUID userId = UUID.randomUUID();
        Users user = user(userId, "seeker@example.com", UserRole.JOB_SEEKER);
        Notification notification = Notification.builder().id(UUID.randomUUID()).build();
        Page<Notification> page = new PageImpl<>(List.of(notification));

        when(userRepository.findByEmail("seeker@example.com")).thenReturn(Optional.of(user));
        when(notificationRepository.findByUser_IdOrderByCreatedAtDesc(eq(userId), any(Pageable.class)))
                .thenReturn(page);
        when(notificationMapper.toResponse(notification)).thenReturn(
                NotificationResponse.builder().id(notification.getId()).read(false).build());

        var response = notificationService.findMyNotifications("seeker@example.com", 0, 20);

        assertThat(response.getContent()).hasSize(1);
    }

    @Test
    void markAsRead_updatesNotification() {
        UUID userId = UUID.randomUUID();
        UUID notificationId = UUID.randomUUID();
        Users user = user(userId, "seeker@example.com", UserRole.JOB_SEEKER);
        Notification notification = Notification.builder()
                .id(notificationId)
                .user(user)
                .read(false)
                .build();

        when(userRepository.findByEmail("seeker@example.com")).thenReturn(Optional.of(user));
        when(notificationRepository.findByIdAndUser_Id(notificationId, userId)).thenReturn(Optional.of(notification));
        when(notificationRepository.save(notification)).thenReturn(notification);
        when(notificationMapper.toResponse(notification)).thenReturn(
                NotificationResponse.builder().id(notificationId).read(true).build());

        var response = notificationService.markAsRead("seeker@example.com", notificationId);

        assertThat(response.isRead()).isTrue();
        assertThat(notification.isRead()).isTrue();
    }

    @Test
    void markAsRead_throwsWhenNotFound() {
        UUID userId = UUID.randomUUID();
        Users user = user(userId, "seeker@example.com", UserRole.JOB_SEEKER);

        when(userRepository.findByEmail("seeker@example.com")).thenReturn(Optional.of(user));
        when(notificationRepository.findByIdAndUser_Id(any(), eq(userId))).thenReturn(Optional.empty());

        assertThatThrownBy(() -> notificationService.markAsRead("seeker@example.com", UUID.randomUUID()))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    private Users user(UUID id, String email, UserRole role) {
        return Users.builder()
                .id(id)
                .email(email)
                .role(role)
                .status(UserStatus.ACTIVE)
                .emailVerified(true)
                .build();
    }
}
