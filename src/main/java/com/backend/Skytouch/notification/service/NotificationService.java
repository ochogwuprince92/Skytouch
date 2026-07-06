package com.backend.Skytouch.notification.service;

import com.backend.Skytouch.application.entity.JobApplication;
import com.backend.Skytouch.authentication.service.EmailService;
import com.backend.Skytouch.common.apimodel.PageResponse;
import com.backend.Skytouch.common.enums.ApplicationStatus;
import com.backend.Skytouch.common.enums.NotificationType;
import com.backend.Skytouch.common.exception.ResourceNotFoundException;
import com.backend.Skytouch.common.mapper.NotificationMapper;
import com.backend.Skytouch.common.util.PaginationUtils;
import com.backend.Skytouch.company.entity.Company;
import com.backend.Skytouch.employer.repository.EmployerRepository;
import com.backend.Skytouch.interview.entity.Interview;
import com.backend.Skytouch.job.entity.Job;
import com.backend.Skytouch.messaging.entity.ApplicationMessage;
import com.backend.Skytouch.notification.apimodel.NotificationResponse;
import com.backend.Skytouch.notification.apimodel.UnreadCountResponse;
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

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final NotificationMapper notificationMapper;
    private final UserRepository userRepository;
    private final EmployerRepository employerRepository;
    private final EmailService emailService;

    @Transactional
    public void notifyOnApplicationSubmitted(JobApplication application) {
        Job job = application.getJob();
        Company company = job.getCompany();
        Users seekerUser = application.getJobSeeker().getUser();
        String seekerName = application.getSeekerName();
        String jobTitle = job.getTitle();
        String companyName = company.getName();

        saveNotification(
                seekerUser,
                NotificationType.APPLICATION_SUBMITTED,
                "Application submitted",
                "Your application for \"" + jobTitle + "\" at " + companyName + " was submitted.",
                application.getId());
        emailService.sendApplicationSubmittedConfirmation(seekerUser.getEmail(), jobTitle, companyName);

        employerRepository.findByCompany_Id(company.getId()).ifPresent(employer -> {
            Users employerUser = employer.getUser();
            saveNotification(
                    employerUser,
                    NotificationType.NEW_APPLICATION,
                    "New application received",
                    seekerName + " applied for \"" + jobTitle + "\".",
                    application.getId());
            emailService.sendNewApplicationAlert(employerUser.getEmail(), seekerName, jobTitle);
        });
    }

    @Transactional
    public void notifyOnStatusUpdated(JobApplication application) {
        Job job = application.getJob();
        Users seekerUser = application.getJobSeeker().getUser();
        String jobTitle = job.getTitle();
        ApplicationStatus status = application.getStatus();
        String statusLabel = formatStatus(status);

        saveNotification(
                seekerUser,
                NotificationType.APPLICATION_STATUS_UPDATED,
                "Application status updated",
                "Your application for \"" + jobTitle + "\" is now " + statusLabel + ".",
                application.getId());
        emailService.sendApplicationStatusUpdate(seekerUser.getEmail(), jobTitle, statusLabel);
    }

    @Transactional
    public void notifyOnInterviewScheduled(Interview interview) {
        JobApplication application = interview.getApplication();
        Job job = application.getJob();
        Users seekerUser = application.getJobSeeker().getUser();
        String jobTitle = job.getTitle();
        String when = interview.getScheduledAt().toString();

        saveNotification(
                seekerUser,
                NotificationType.INTERVIEW_SCHEDULED,
                "Interview scheduled",
                "Your interview for \"" + jobTitle + "\" is scheduled for " + when + ".",
                application.getId());
        emailService.sendInterviewScheduled(seekerUser.getEmail(), jobTitle, when);
    }

    @Transactional
    public void notifyOnInterviewUpdated(Interview interview) {
        JobApplication application = interview.getApplication();
        Job job = application.getJob();
        Users seekerUser = application.getJobSeeker().getUser();
        String jobTitle = job.getTitle();
        String details = "Status: " + interview.getStatus() + ", scheduled: " + interview.getScheduledAt();

        saveNotification(
                seekerUser,
                NotificationType.INTERVIEW_UPDATED,
                "Interview updated",
                "Your interview for \"" + jobTitle + "\" has been updated.",
                application.getId());
        emailService.sendInterviewUpdated(seekerUser.getEmail(), jobTitle, details);
    }

    @Transactional
    public void notifyOnNewMessage(ApplicationMessage message) {
        JobApplication application = message.getApplication();
        Job job = application.getJob();
        Users sender = message.getSender();
        String jobTitle = job.getTitle();
        Users recipient = resolveMessageRecipient(application, sender);

        saveNotification(
                recipient,
                NotificationType.NEW_MESSAGE,
                "New message",
                "You have a new message about \"" + jobTitle + "\".",
                application.getId());
        emailService.sendNewMessageAlert(recipient.getEmail(), jobTitle);
    }

    @Transactional
    public void notifyOnOfferExtended(com.backend.Skytouch.offer.entity.JobOffer offer) {
        JobApplication application = offer.getApplication();
        Job job = application.getJob();
        Users seekerUser = application.getJobSeeker().getUser();
        String jobTitle = job.getTitle();
        String companyName = job.getCompany().getName();

        saveNotification(
                seekerUser,
                NotificationType.OFFER_EXTENDED,
                "Job offer received",
                "You received an offer for \"" + jobTitle + "\" at " + companyName + ".",
                application.getId());
        emailService.sendOfferExtended(seekerUser.getEmail(), jobTitle, companyName);
    }

    @Transactional
    public void notifyOnOfferAccepted(com.backend.Skytouch.offer.entity.JobOffer offer) {
        JobApplication application = offer.getApplication();
        Job job = application.getJob();
        String jobTitle = job.getTitle();
        String seekerName = application.getSeekerName();

        employerRepository.findByCompany_Id(job.getCompany().getId()).ifPresent(employer -> {
            Users employerUser = employer.getUser();
            saveNotification(
                    employerUser,
                    NotificationType.OFFER_ACCEPTED,
                    "Offer accepted",
                    seekerName + " accepted your offer for \"" + jobTitle + "\".",
                    application.getId());
            emailService.sendOfferAccepted(employerUser.getEmail(), seekerName, jobTitle);
        });

        Users seekerUser = application.getJobSeeker().getUser();
        saveNotification(
                seekerUser,
                NotificationType.HIRED,
                "Congratulations — you're hired!",
                "You are now hired for \"" + jobTitle + "\".",
                application.getId());
        emailService.sendHiredConfirmation(seekerUser.getEmail(), jobTitle);
    }

    @Transactional
    public void notifyOnOfferDeclined(com.backend.Skytouch.offer.entity.JobOffer offer) {
        JobApplication application = offer.getApplication();
        Job job = application.getJob();
        String jobTitle = job.getTitle();
        String seekerName = application.getSeekerName();

        employerRepository.findByCompany_Id(job.getCompany().getId()).ifPresent(employer -> {
            Users employerUser = employer.getUser();
            saveNotification(
                    employerUser,
                    NotificationType.OFFER_DECLINED,
                    "Offer declined",
                    seekerName + " declined your offer for \"" + jobTitle + "\".",
                    application.getId());
            emailService.sendOfferDeclined(employerUser.getEmail(), seekerName, jobTitle);
        });
    }

    @Transactional
    public void notifyOnJobAlertMatch(Users seeker, Job job) {
        String jobTitle = job.getTitle();
        String companyName = job.getCompany().getName();

        saveNotification(
                seeker,
                NotificationType.JOB_ALERT_MATCH,
                "New job matches your alert",
                "\"" + jobTitle + "\" at " + companyName + " matches one of your job alerts.",
                null);
        emailService.sendJobAlertMatch(seeker.getEmail(), jobTitle, companyName);
    }

    @Transactional
    public void notifyOnJobAlertDigest(Users seeker, List<Job> jobs) {
        if (jobs.isEmpty()) {
            return;
        }

        List<String> summaries = jobs.stream()
                .map(job -> "\"" + job.getTitle() + "\" at " + job.getCompany().getName())
                .toList();

        String preview = summaries.size() == 1
                ? summaries.get(0)
                : summaries.size() + " new jobs including " + summaries.get(0);

        saveNotification(
                seeker,
                NotificationType.JOB_ALERT_DIGEST,
                "Your job alert digest",
                preview + " match your saved alerts.",
                null);
        emailService.sendJobAlertDigest(seeker.getEmail(), summaries);
    }

    @Transactional(readOnly = true)
    public PageResponse<NotificationResponse> findMyNotifications(String email, int page, int size) {
        Users user = getUserByEmail(email);
        Pageable pageable = PaginationUtils.pageable(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<Notification> results = notificationRepository.findByUser_IdOrderByCreatedAtDesc(user.getId(), pageable);
        return PaginationUtils.mapPage(results, notificationMapper::toResponse);
    }

    @Transactional
    public NotificationResponse markAsRead(String email, UUID notificationId) {
        Users user = getUserByEmail(email);
        Notification notification = notificationRepository.findByIdAndUser_Id(notificationId, user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Notification not found: " + notificationId));
        notification.setRead(true);
        return notificationMapper.toResponse(notificationRepository.save(notification));
    }

    @Transactional
    public void markAllAsRead(String email) {
        Users user = getUserByEmail(email);
        notificationRepository.markAllReadForUser(user.getId());
    }

    @Transactional(readOnly = true)
    public UnreadCountResponse countUnread(String email) {
        Users user = getUserByEmail(email);
        return UnreadCountResponse.builder()
                .unreadCount(notificationRepository.countByUser_IdAndReadFalse(user.getId()))
                .build();
    }

    private Users getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + email));
    }

    private void saveNotification(
            Users user,
            NotificationType type,
            String title,
            String message,
            UUID applicationId) {
        Notification notification = Notification.builder()
                .user(user)
                .type(type)
                .title(title)
                .message(message)
                .applicationId(applicationId)
                .read(false)
                .build();
        notificationRepository.save(notification);
    }

    private String formatStatus(ApplicationStatus status) {
        return switch (status) {
            case SUBMITTED -> "submitted";
            case REVIEWING -> "under review";
            case SHORTLISTED -> "shortlisted";
            case INTERVIEW_SCHEDULED -> "interview scheduled";
            case OFFER_EXTENDED -> "offer extended";
            case OFFER_DECLINED -> "offer declined";
            case HIRED -> "hired";
            case REJECTED -> "rejected";
            case WITHDRAWN -> "withdrawn";
        };
    }

    private Users resolveMessageRecipient(JobApplication application, Users sender) {
        Users seekerUser = application.getJobSeeker().getUser();
        if (!seekerUser.getId().equals(sender.getId())) {
            return seekerUser;
        }
        return employerRepository.findByCompany_Id(application.getJob().getCompany().getId())
                .map(employer -> employer.getUser())
                .orElse(seekerUser);
    }
}
