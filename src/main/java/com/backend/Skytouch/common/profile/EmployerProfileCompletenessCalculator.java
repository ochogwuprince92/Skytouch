package com.backend.Skytouch.common.profile;

import com.backend.Skytouch.employer.entity.Employer;
import com.backend.Skytouch.user.entity.Users;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

@Component
public class EmployerProfileCompletenessCalculator {

    public ProfileCompleteness calculate(Users user, Employer profile, boolean companyLinked) {
        List<ProfileStep> steps = new ArrayList<>();

        steps.add(ProfileStep.builder()
                .key("email_verified")
                .label("Verify email address")
                .complete(Boolean.TRUE.equals(user.getEmailVerified()))
                .build());

        steps.add(ProfileStep.builder()
                .key("basic_info")
                .label("Complete basic information")
                .complete(profile != null
                        && StringUtils.hasText(profile.getFirstName())
                        && StringUtils.hasText(profile.getLastName())
                        && StringUtils.hasText(profile.getPhone()))
                .build());

        steps.add(ProfileStep.builder()
                .key("company_profile")
                .label("Add company name")
                .complete(profile != null && StringUtils.hasText(profile.getCompanyName()))
                .build());

        steps.add(ProfileStep.builder()
                .key("role_details")
                .label("Add your job title")
                .complete(profile != null && StringUtils.hasText(profile.getJobTitle()))
                .build());

        steps.add(ProfileStep.builder()
                .key("company_linked")
                .label("Link or create a company profile")
                .complete(companyLinked)
                .build());

        int complete = (int) steps.stream().filter(ProfileStep::isComplete).count();
        int percent = steps.isEmpty() ? 0 : (complete * 100) / steps.size();

        return ProfileCompleteness.builder()
                .percentComplete(percent)
                .steps(steps)
                .build();
    }
}
