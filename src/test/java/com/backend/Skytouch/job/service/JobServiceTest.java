package com.backend.Skytouch.job.service;

import com.backend.Skytouch.common.enums.CompanyStatus;
import com.backend.Skytouch.common.enums.EmploymentType;
import com.backend.Skytouch.common.enums.JobStatus;
import com.backend.Skytouch.common.enums.WorkMode;
import com.backend.Skytouch.common.exception.BadRequestException;
import com.backend.Skytouch.common.exception.ResourceNotFoundException;
import com.backend.Skytouch.common.mapper.JobMapper;
import com.backend.Skytouch.company.entity.Company;
import com.backend.Skytouch.company.service.CompanyService;
import com.backend.Skytouch.job.apimodel.JobCreateRequest;
import com.backend.Skytouch.job.apimodel.JobUpdateRequest;
import com.backend.Skytouch.job.entity.Job;
import com.backend.Skytouch.job.repository.JobRepository;
import com.backend.Skytouch.jobalert.service.JobAlertService;
import com.backend.Skytouch.savedjob.service.SavedJobService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
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
class JobServiceTest {

    @Mock
    private JobRepository jobRepository;

    @Mock
    private CompanyService companyService;

    @Mock
    private JobMapper jobMapper;

    @Mock
    private SavedJobService savedJobService;

    @Mock
    private JobAlertService jobAlertService;

    @InjectMocks
    private JobService jobService;

    @Test
    void create_savesDraftJobForLinkedCompany() {
        Company company = Company.builder().id(UUID.randomUUID()).name("Acme Ltd").build();
        JobCreateRequest request = new JobCreateRequest();
        request.setTitle("Backend Engineer");
        request.setDescription("Build APIs");
        request.setEmploymentType(EmploymentType.FULL_TIME);
        request.setWorkMode(WorkMode.HYBRID);

        Job job = Job.builder()
                .id(UUID.randomUUID())
                .company(company)
                .title("Backend Engineer")
                .status(JobStatus.DRAFT)
                .build();

        when(companyService.getLinkedCompany("employer@example.com")).thenReturn(company);
        when(jobMapper.toEntity(request, company)).thenReturn(job);
        when(jobRepository.save(job)).thenReturn(job);
        when(jobMapper.toResponse(job)).thenReturn(
                com.backend.Skytouch.job.apimodel.JobResponse.builder()
                        .id(job.getId())
                        .title("Backend Engineer")
                        .status(JobStatus.DRAFT)
                        .build());

        var response = jobService.create("employer@example.com", request);

        assertThat(response.getStatus()).isEqualTo(JobStatus.DRAFT);
        verify(jobRepository).save(job);
    }

    @Test
    void create_rejectsInvalidSalaryRange() {
        Company company = Company.builder().id(UUID.randomUUID()).name("Acme Ltd").build();
        JobCreateRequest request = new JobCreateRequest();
        request.setTitle("Backend Engineer");
        request.setDescription("Build APIs");
        request.setEmploymentType(EmploymentType.FULL_TIME);
        request.setWorkMode(WorkMode.HYBRID);
        request.setSalaryMin(500000L);
        request.setSalaryMax(100000L);

        when(companyService.getLinkedCompany("employer@example.com")).thenReturn(company);

        assertThatThrownBy(() -> jobService.create("employer@example.com", request))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Minimum salary");
        verify(jobRepository, never()).save(any());
    }

    @Test
    void publish_movesDraftToActive() {
        UUID companyId = UUID.randomUUID();
        Company company = Company.builder().id(companyId).name("Acme Ltd").build();
        UUID jobId = UUID.randomUUID();
        Job job = Job.builder().id(jobId).company(company).status(JobStatus.DRAFT).build();

        when(companyService.getLinkedCompany("employer@example.com")).thenReturn(company);
        when(jobRepository.findById(jobId)).thenReturn(Optional.of(job));
        when(jobRepository.save(job)).thenReturn(job);
        when(jobMapper.toResponse(job)).thenReturn(
                com.backend.Skytouch.job.apimodel.JobResponse.builder()
                        .id(jobId)
                        .status(JobStatus.ACTIVE)
                        .build());

        var response = jobService.publish("employer@example.com", jobId);

        assertThat(response.getStatus()).isEqualTo(JobStatus.ACTIVE);
        assertThat(job.getStatus()).isEqualTo(JobStatus.ACTIVE);
        assertThat(job.getPublishedAt()).isNotNull();
        verify(jobAlertService).notifyMatchingSeekers(job);
    }

    @Test
    void publish_rejectsNonDraftJob() {
        UUID companyId = UUID.randomUUID();
        Company company = Company.builder().id(companyId).name("Acme Ltd").build();
        UUID jobId = UUID.randomUUID();
        Job job = Job.builder().id(jobId).company(company).status(JobStatus.ACTIVE).build();

        when(companyService.getLinkedCompany("employer@example.com")).thenReturn(company);
        when(jobRepository.findById(jobId)).thenReturn(Optional.of(job));

        assertThatThrownBy(() -> jobService.publish("employer@example.com", jobId))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Only draft jobs");
    }

    @Test
    void close_movesActiveToClosed() {
        UUID companyId = UUID.randomUUID();
        Company company = Company.builder().id(companyId).name("Acme Ltd").build();
        UUID jobId = UUID.randomUUID();
        Job job = Job.builder().id(jobId).company(company).status(JobStatus.ACTIVE).build();

        when(companyService.getLinkedCompany("employer@example.com")).thenReturn(company);
        when(jobRepository.findById(jobId)).thenReturn(Optional.of(job));
        when(jobRepository.save(job)).thenReturn(job);
        when(jobMapper.toResponse(job)).thenReturn(
                com.backend.Skytouch.job.apimodel.JobResponse.builder()
                        .id(jobId)
                        .status(JobStatus.CLOSED)
                        .build());

        var response = jobService.close("employer@example.com", jobId);

        assertThat(response.getStatus()).isEqualTo(JobStatus.CLOSED);
        assertThat(job.getClosedAt()).isNotNull();
    }

    @Test
    void update_rejectsClosedJob() {
        UUID companyId = UUID.randomUUID();
        Company company = Company.builder().id(companyId).name("Acme Ltd").build();
        UUID jobId = UUID.randomUUID();
        Job job = Job.builder().id(jobId).company(company).status(JobStatus.CLOSED).build();

        when(companyService.getLinkedCompany("employer@example.com")).thenReturn(company);
        when(jobRepository.findById(jobId)).thenReturn(Optional.of(job));

        assertThatThrownBy(() -> jobService.update("employer@example.com", jobId, new JobUpdateRequest()))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Closed jobs");
    }

    @Test
    void search_returnsActiveJobsOnly() {
        Company company = Company.builder().id(UUID.randomUUID()).name("Acme Ltd").industry("Technology").build();
        Job job = Job.builder()
                .id(UUID.randomUUID())
                .company(company)
                .title("Backend Engineer")
                .status(JobStatus.ACTIVE)
                .build();
        Page<Job> page = new PageImpl<>(List.of(job));

        when(jobRepository.search(
                eq(JobStatus.ACTIVE),
                eq("engineer"),
                eq(EmploymentType.FULL_TIME),
                eq(WorkMode.REMOTE),
                eq("Lagos"),
                eq("Technology"),
                eq(CompanyStatus.ACTIVE),
                any(Pageable.class))).thenReturn(page);
        when(jobMapper.toResponse(job)).thenReturn(
                com.backend.Skytouch.job.apimodel.JobResponse.builder()
                        .title("Backend Engineer")
                        .status(JobStatus.ACTIVE)
                        .build());

        var response = jobService.search(
                "engineer", EmploymentType.FULL_TIME, WorkMode.REMOTE, "Lagos", "Technology", 0, 20);

        assertThat(response.getContent()).hasSize(1);
        assertThat(response.getTotalElements()).isEqualTo(1);
        assertThat(response.getPage()).isZero();
        assertThat(response.getContent().get(0).getStatus()).isEqualTo(JobStatus.ACTIVE);
    }

    @Test
    void findById_hidesDraftFromOtherEmployers() {
        UUID jobId = UUID.randomUUID();
        Company company = Company.builder().id(UUID.randomUUID()).name("Acme Ltd").build();
        Job job = Job.builder().id(jobId).company(company).status(JobStatus.DRAFT).build();

        when(jobRepository.findById(jobId)).thenReturn(Optional.of(job));
        when(companyService.getLinkedCompany("other@example.com"))
                .thenReturn(Company.builder().id(UUID.randomUUID()).build());

        assertThatThrownBy(() -> jobService.findById(jobId, "other@example.com", true, false))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}
