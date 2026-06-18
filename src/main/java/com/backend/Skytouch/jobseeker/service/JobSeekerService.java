package com.backend.Skytouch.jobseeker.service;

import com.backend.Skytouch.common.enums.UserRole;
import com.backend.Skytouch.common.enums.UserStatus;
import com.backend.Skytouch.common.exception.BadRequestException;
import com.backend.Skytouch.common.exception.ConflictException;
import com.backend.Skytouch.common.exception.ResourceNotFoundException;
import com.backend.Skytouch.common.mapper.JobSeekerMapper;
import com.backend.Skytouch.common.utils.StringUtils;
import com.backend.Skytouch.jobseeker.apimodel.JobSeekerResponse;
import com.backend.Skytouch.jobseeker.apimodel.RegisterJobSeekerRequest;
import com.backend.Skytouch.jobseeker.repository.JobSeekerRepository;
import com.backend.Skytouch.user.entity.Users;
import com.backend.Skytouch.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class JobSeekerService {

    private static final UserRole JOB_SEEKER_ROLE = UserRole.JOB_SEEKER;

    private final JobSeekerRepository jobSeekerRepository;
    private final UserRepository userRepository;
    private final JobSeekerMapper jobSeekerMapper;

    @Transactional(readOnly = true)
    public List<JobSeekerResponse> findAll() {
        return jobSeekerRepository.findByRole(JOB_SEEKER_ROLE).stream()
                .map(jobSeekerMapper::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public JobSeekerResponse findById(UUID id) {
        Users user = jobSeekerRepository.findByIdAndRole(id, JOB_SEEKER_ROLE)
                .orElseThrow(() -> new ResourceNotFoundException("Job seeker not found: " + id));
        return jobSeekerMapper.toResponse(user);
    }

    @Transactional
    public JobSeekerResponse register(RegisterJobSeekerRequest request) {

        if (StringUtils.isBlank(request.getEmail()) || StringUtils.isBlank(request.getPassword())) {
            throw new BadRequestException("Email and password are required");
        }

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new ConflictException("Email already registered: " + request.getEmail());
        }

        Users user = Users.builder()
                .email(request.getEmail())
                .password(request.getPassword())
                .role(JOB_SEEKER_ROLE)
                .status(UserStatus.PENDING)
                .build();

        return jobSeekerMapper.toResponse(jobSeekerRepository.save(user));
    }
}
