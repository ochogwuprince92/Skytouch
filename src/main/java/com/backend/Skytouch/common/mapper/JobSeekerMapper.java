package com.backend.Skytouch.common.mapper;

import com.backend.Skytouch.authentication.apimodel.RegisterRequest;
import com.backend.Skytouch.common.enums.Gender;
import com.backend.Skytouch.common.enums.JobRole;
import com.backend.Skytouch.common.enums.Qualification;
import com.backend.Skytouch.common.enums.UserStatus;
import com.backend.Skytouch.jobseeker.apimodel.JobSeekerKycRequest;
import com.backend.Skytouch.jobseeker.apimodel.JobSeekerOnboardingRequest;
import com.backend.Skytouch.jobseeker.apimodel.JobSeekerResponse;
import com.backend.Skytouch.jobseeker.entity.JobSeeker;
import com.backend.Skytouch.user.entity.Users;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
public class JobSeekerMapper {

    public JobSeeker toEntity(RegisterRequest request, Users user) {
        return JobSeeker.builder()
                .user(user)
                .status(UserStatus.PENDING)
                .firstName(request.getFirstName())
                .middleName(request.getMiddleName())
                .lastName(request.getLastName())
                .phone(request.getPhone())
                .openToWork(false)
                .build();
    }

    public void applyOnboarding(JobSeeker profile, JobSeekerOnboardingRequest request, String cvUrl) {
        if (request.getJob() != null) {
            profile.setJob(JobRole.valueOf(request.getJob().toUpperCase()));
        }
        if (request.getQualification() != null) {
            profile.setQualification(Qualification.valueOf(request.getQualification().toUpperCase()));
        }
        if (cvUrl != null) {
            profile.setCvUrl(cvUrl);
        }
        if (request.getAbout() != null) {
            profile.setAbout(request.getAbout());
        }
        if (request.getOpenToWork() != null) {
            profile.setOpenToWork(request.getOpenToWork());
        }
    }

    public void applyKyc(JobSeeker profile, JobSeekerKycRequest request) {
        if (request.getNin() != null) {
            profile.setNin(request.getNin());
        }
        if (request.getBirthday() != null) {
            profile.setBirthday(request.getBirthday());
        }
        if (request.getGender() != null) {
            profile.setGender(Gender.valueOf(request.getGender().toUpperCase()));
        }
        if (request.getAddressNo() != null) {
            profile.setAddressNo(request.getAddressNo());
        }
        if (request.getAddressLine() != null) {
            profile.setAddressLine(request.getAddressLine());
        }
        if (request.getAddressLga() != null) {
            profile.setAddressLga(request.getAddressLga());
        }
        if (request.getAddressState() != null) {
            profile.setAddressState(request.getAddressState());
        }
    }

    public JobSeekerResponse toResponse(Users user, JobSeeker profile) {
        JobSeekerResponse.JobSeekerResponseBuilder builder = JobSeekerResponse.builder()
                .id(user.getId())
                .email(user.getEmail())
                .status(user.getStatus())
                .emailVerified(user.getEmailVerified())
                .active(user.getActive())
                .createdAt(user.getCreatedAt());

        if (profile != null) {
            builder
                    .firstName(profile.getFirstName())
                    .middleName(profile.getMiddleName())
                    .lastName(profile.getLastName())
                    .phone(profile.getPhone())
                    .job(profile.getJob() != null ? profile.getJob().name() : null)
                    .qualification(profile.getQualification() != null ? profile.getQualification().name() : null)
                    .cv(profile.getCvUrl())
                    .about(profile.getAbout())
                    .openToWork(profile.getOpenToWork())
                    .addressState(profile.getAddressState())
                    .addressLga(profile.getAddressLga())
                    .addressLine(profile.getAddressLine())
                    .nin(profile.getNin())
                    .birthday(profile.getBirthday())
                    .gender(profile.getGender() != null ? profile.getGender().name() : null)
                    .addressNo(profile.getAddressNo());
        }

        return builder.build();
    }
}
