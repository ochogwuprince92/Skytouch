package com.backend.Skytouch.savedjob.service;

import com.backend.Skytouch.common.enums.JobStatus;
import com.backend.Skytouch.common.enums.UserRole;
import com.backend.Skytouch.common.enums.UserStatus;
import com.backend.Skytouch.common.exception.ConflictException;
import com.backend.Skytouch.common.mapper.JobMapper;
import com.backend.Skytouch.job.entity.Job;
import com.backend.Skytouch.job.repository.JobRepository;
import com.backend.Skytouch.jobseeker.entity.JobSeeker;
import com.backend.Skytouch.jobseeker.repository.JobSeekerRepository;
import com.backend.Skytouch.savedjob.entity.SavedJob;
import com.backend.Skytouch.savedjob.repository.SavedJobRepository;
import com.backend.Skytouch.user.entity.Users;
import com.backend.Skytouch.user.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SavedJobServiceTest {

    @Mock
    private SavedJobRepository savedJobRepository;

    @Mock
    private JobRepository jobRepository;

    @Mock
    private JobSeekerRepository jobSeekerRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private JobMapper jobMapper;

    @InjectMocks
    private SavedJobService savedJobService;

    @Test
    void save_rejectsDuplicate() {
        UUID jobId = UUID.randomUUID();
        UUID seekerId = UUID.randomUUID();
        Users user = seekerUser(seekerId);
        JobSeeker seeker = JobSeeker.builder().id(seekerId).user(user).build();
        Job job = Job.builder().id(jobId).status(JobStatus.ACTIVE).build();

        when(userRepository.findByEmailAndRole("seeker@example.com", UserRole.JOB_SEEKER))
                .thenReturn(Optional.of(user));
        when(jobSeekerRepository.findByUser_Id(seekerId)).thenReturn(Optional.of(seeker));
        when(jobRepository.findById(jobId)).thenReturn(Optional.of(job));
        when(savedJobRepository.existsByJobSeeker_User_EmailAndJob_Id("seeker@example.com", jobId))
                .thenReturn(true);

        assertThatThrownBy(() -> savedJobService.save("seeker@example.com", jobId))
                .isInstanceOf(ConflictException.class);
    }

    @Test
    void save_persistsActiveJob() {
        UUID jobId = UUID.randomUUID();
        UUID seekerId = UUID.randomUUID();
        Users user = seekerUser(seekerId);
        JobSeeker seeker = JobSeeker.builder().id(seekerId).user(user).build();
        Job job = Job.builder().id(jobId).status(JobStatus.ACTIVE).build();

        when(userRepository.findByEmailAndRole("seeker@example.com", UserRole.JOB_SEEKER))
                .thenReturn(Optional.of(user));
        when(jobSeekerRepository.findByUser_Id(seekerId)).thenReturn(Optional.of(seeker));
        when(jobRepository.findById(jobId)).thenReturn(Optional.of(job));
        when(savedJobRepository.existsByJobSeeker_User_EmailAndJob_Id("seeker@example.com", jobId))
                .thenReturn(false);

        savedJobService.save("seeker@example.com", jobId);

        verify(savedJobRepository).save(any(SavedJob.class));
    }

    private Users seekerUser(UUID id) {
        return Users.builder()
                .id(id)
                .email("seeker@example.com")
                .role(UserRole.JOB_SEEKER)
                .status(UserStatus.ACTIVE)
                .emailVerified(true)
                .build();
    }
}
