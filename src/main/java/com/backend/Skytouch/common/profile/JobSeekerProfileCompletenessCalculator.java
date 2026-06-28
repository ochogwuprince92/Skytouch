package com.backend.Skytouch.common.profile;

import com.backend.Skytouch.jobseeker.entity.JobSeeker;
import com.backend.Skytouch.user.entity.Users;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

@Component
public class JobSeekerProfileCompletenessCalculator {

    public ProfileCompleteness calculate(Users user, JobSeeker profile) {
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
                .key("career_profile")
                .label("Add job, qualification, and CV")
                .complete(profile != null
                        && StringUtils.hasText(profile.getJob())
                        && StringUtils.hasText(profile.getQualification())
                        && StringUtils.hasText(profile.getCvUrl()))
                .build());

        steps.add(ProfileStep.builder()
                .key("kyc")
                .label("Complete KYC (NIN, birthday, gender, address)")
                .complete(profile != null
                        && StringUtils.hasText(profile.getNin())
                        && profile.getBirthday() != null
                        && StringUtils.hasText(profile.getGender())
                        && StringUtils.hasText(profile.getAddressLine()))
                .build());

        int complete = (int) steps.stream().filter(ProfileStep::isComplete).count();
        int percent = steps.isEmpty() ? 0 : (complete * 100) / steps.size();

        return ProfileCompleteness.builder()
                .percentComplete(percent)
                .steps(steps)
                .build();
    }
}
