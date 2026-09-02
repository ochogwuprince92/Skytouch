package com.backend.Skytouch.common.mapper;

import com.backend.Skytouch.authentication.apimodel.RegisterRequest;
import com.backend.Skytouch.common.enums.UserStatus;
import com.backend.Skytouch.employer.apimodel.EmployerProfileRequest;
import com.backend.Skytouch.employer.apimodel.EmployerResponse;
import com.backend.Skytouch.company.entity.Company;
import com.backend.Skytouch.employer.entity.Employer;
import com.backend.Skytouch.user.entity.Users;
import org.springframework.stereotype.Component;

@Component
public class EmployerMapper {

    public Employer toEntity(RegisterRequest request, Users user) {
        return Employer.builder()
                .user(user)
                .status(UserStatus.PENDING)
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .phone(request.getPhone())
                .companyName(request.getCompanyName())
                .build();
    }

    public void applyProfile(Employer profile, EmployerProfileRequest request) {
        if (request.getFirstName() != null) {
            profile.setFirstName(request.getFirstName());
        }
        if (request.getLastName() != null) {
            profile.setLastName(request.getLastName());
        }
        if (request.getCompanyName() != null) {
            profile.setCompanyName(request.getCompanyName());
        }
        if (request.getJobTitle() != null) {
            profile.setJobTitle(request.getJobTitle());
        }
    }

    public EmployerResponse toResponse(Users user, Employer profile) {
        EmployerResponse.EmployerResponseBuilder builder = EmployerResponse.builder()
                .id(user.getId())
                .email(user.getEmail())
                .status(user.getStatus())
                .emailVerified(user.getEmailVerified())
                .active(user.getActive())
                .createdAt(user.getCreatedAt());

        if (profile != null) {
            Company company = profile.getCompany();
            builder
                    .firstName(profile.getFirstName())
                    .lastName(profile.getLastName())
                    .phone(profile.getPhone())
                    .companyName(company != null ? company.getName() : profile.getCompanyName())
                    .companyId(company != null ? company.getId() : null)
                    .jobTitle(profile.getJobTitle());
        }

        return builder.build();
    }
}
