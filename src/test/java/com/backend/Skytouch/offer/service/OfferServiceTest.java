package com.backend.Skytouch.offer.service;

import com.backend.Skytouch.application.entity.JobApplication;
import com.backend.Skytouch.application.repository.JobApplicationRepository;
import com.backend.Skytouch.common.enums.ApplicationStatus;
import com.backend.Skytouch.common.enums.InterviewStatus;
import com.backend.Skytouch.common.enums.OfferStatus;
import com.backend.Skytouch.common.enums.UserRole;
import com.backend.Skytouch.common.enums.UserStatus;
import com.backend.Skytouch.common.exception.BadRequestException;
import com.backend.Skytouch.common.mapper.OfferMapper;
import com.backend.Skytouch.company.entity.Company;
import com.backend.Skytouch.company.service.CompanyService;
import com.backend.Skytouch.interview.entity.Interview;
import com.backend.Skytouch.interview.repository.InterviewRepository;
import com.backend.Skytouch.job.entity.Job;
import com.backend.Skytouch.job.repository.JobRepository;
import com.backend.Skytouch.jobseeker.entity.JobSeeker;
import com.backend.Skytouch.notification.service.NotificationService;
import com.backend.Skytouch.offer.apimodel.OfferCreateRequest;
import com.backend.Skytouch.offer.entity.JobOffer;
import com.backend.Skytouch.offer.repository.JobOfferRepository;
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
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OfferServiceTest {

    @Mock
    private JobOfferRepository offerRepository;

    @Mock
    private JobApplicationRepository applicationRepository;

    @Mock
    private InterviewRepository interviewRepository;

    @Mock
    private JobRepository jobRepository;

    @Mock
    private OfferMapper offerMapper;

    @Mock
    private CompanyService companyService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private NotificationService notificationService;

    @InjectMocks
    private OfferService offerService;

    @Test
    void extendOffer_requiresCompletedInterview() {
        UUID applicationId = UUID.randomUUID();
        UUID companyId = UUID.randomUUID();
        Company company = Company.builder().id(companyId).build();
        Job job = Job.builder().company(company).build();
        Users seekerUser = Users.builder().id(UUID.randomUUID()).email("seeker@example.com").build();
        JobSeeker seeker = JobSeeker.builder().user(seekerUser).build();
        JobApplication application = JobApplication.builder()
                .id(applicationId)
                .job(job)
                .jobSeeker(seeker)
                .status(ApplicationStatus.INTERVIEW_SCHEDULED)
                .build();
        Interview interview = Interview.builder().status(InterviewStatus.SCHEDULED).build();

        when(applicationRepository.findById(applicationId)).thenReturn(Optional.of(application));
        when(companyService.getLinkedCompany("employer@example.com")).thenReturn(company);
        when(interviewRepository.findByApplication_Id(applicationId)).thenReturn(Optional.of(interview));

        assertThatThrownBy(() -> offerService.extendOffer(
                "employer@example.com", applicationId, new OfferCreateRequest()))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("completed");
    }

    @Test
    void acceptOffer_marksApplicationHired() {
        UUID offerId = UUID.randomUUID();
        UUID companyId = UUID.randomUUID();
        Company company = Company.builder().id(companyId).name("Acme").build();
        Job job = Job.builder().company(company).title("Engineer").status(
                com.backend.Skytouch.common.enums.JobStatus.ACTIVE).build();
        Users seekerUser = Users.builder().id(UUID.randomUUID()).email("seeker@example.com").build();
        JobSeeker seeker = JobSeeker.builder().user(seekerUser).build();
        JobApplication application = JobApplication.builder()
                .id(UUID.randomUUID())
                .job(job)
                .jobSeeker(seeker)
                .status(ApplicationStatus.OFFER_EXTENDED)
                .build();
        JobOffer offer = JobOffer.builder()
                .id(offerId)
                .application(application)
                .status(OfferStatus.PENDING)
                .build();

        when(offerRepository.findByIdAndApplication_JobSeeker_User_Email(offerId, "seeker@example.com"))
                .thenReturn(Optional.of(offer));
        when(offerRepository.save(offer)).thenReturn(offer);
        when(applicationRepository.save(application)).thenReturn(application);
        when(jobRepository.save(job)).thenReturn(job);
        when(offerMapper.toResponse(offer)).thenReturn(
                com.backend.Skytouch.offer.apimodel.OfferResponse.builder()
                        .id(offerId)
                        .status(OfferStatus.ACCEPTED)
                        .build());

        var response = offerService.acceptOffer("seeker@example.com", offerId);

        assertThat(response.getStatus()).isEqualTo(OfferStatus.ACCEPTED);
        assertThat(application.getStatus()).isEqualTo(ApplicationStatus.HIRED);
        verify(notificationService).notifyOnOfferAccepted(offer);
    }
}
