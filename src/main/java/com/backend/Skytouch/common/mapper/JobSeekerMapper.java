package com.backend.Skytouch.common.mapper;

import com.backend.Skytouch.authentication.apimodel.OtpSentResponse;
import com.backend.Skytouch.common.enums.UserStatus;
import com.backend.Skytouch.jobseeker.apimodel.JobSeekerKycRequest;
import com.backend.Skytouch.jobseeker.apimodel.JobSeekerOnboardingRequest;
import com.backend.Skytouch.jobseeker.apimodel.JobSeekerResponse;
import com.backend.Skytouch.authentication.apimodel.RegisterJobSeekerRequest;
import com.backend.Skytouch.authentication.apimodel.RegisterJobSeekerResponse;
import com.backend.Skytouch.jobseeker.entity.JobSeeker;
import com.backend.Skytouch.user.entity.Users;
import org.springframework.stereotype.Component;

@Component
public class JobSeekerMapper {

    public JobSeeker toEntity(RegisterJobSeekerRequest request, Users user) {
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
            profile.setJob(request.getJob());
        }
        if (request.getQualification() != null) {
            profile.setQualification(request.getQualification());
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
        if (request.getAddressState() != null) {
            profile.setAddressState(request.getAddressState());
        }
        if (request.getAddressLga() != null) {
            profile.setAddressLga(request.getAddressLga());
        }
        if (request.getAddressLine() != null) {
            profile.setAddressLine(request.getAddressLine());
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
            profile.setGender(request.getGender());
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
                    .job(profile.getJob())
                    .qualification(profile.getQualification())
                    .cv(profile.getCvUrl())
                    .about(profile.getAbout())
                    .openToWork(profile.getOpenToWork())
                    .addressState(profile.getAddressState())
                    .addressLga(profile.getAddressLga())
                    .addressLine(profile.getAddressLine())
                    .nin(profile.getNin())
                    .birthday(profile.getBirthday())
                    .gender(profile.getGender())
                    .addressNo(profile.getAddressNo());
        }

        return builder.build();
    }

    public RegisterJobSeekerResponse toRegisterResponse(Users user, OtpSentResponse verification) {
        return RegisterJobSeekerResponse.builder()
                .id(user.getId())
                .email(user.getEmail())
                .status(user.getStatus())
                .emailVerified(user.getEmailVerified())
                .active(user.getActive())
                .createdAt(user.getCreatedAt())
                .verificationMessage(verification.getMessage())
                .verificationExpiresIn(verification.getExpiresIn())
                .build();
    }
}
