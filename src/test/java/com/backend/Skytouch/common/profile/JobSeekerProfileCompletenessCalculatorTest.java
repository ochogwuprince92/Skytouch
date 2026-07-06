package com.backend.Skytouch.common.profile;

import com.backend.Skytouch.common.enums.UserRole;
import com.backend.Skytouch.common.enums.UserStatus;
import com.backend.Skytouch.jobseeker.entity.JobSeeker;
import com.backend.Skytouch.user.entity.Users;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

class JobSeekerProfileCompletenessCalculatorTest {

    private final JobSeekerProfileCompletenessCalculator calculator =
            new JobSeekerProfileCompletenessCalculator();

    @Test
    void calculate_returnsFullCompleteness_whenAllStepsComplete() {
        Users user = Users.builder()
                .email("seeker@example.com")
                .role(UserRole.JOB_SEEKER)
                .status(UserStatus.ACTIVE)
                .emailVerified(true)
                .build();
        JobSeeker profile = JobSeeker.builder()
                .user(user)
                .status(UserStatus.ACTIVE)
                .firstName("Ada")
                .lastName("Okafor")
                .phone("+2348012345678")
                .job("Engineer")
                .qualification("BSc")
                .cvUrl("https://example.com/cv.pdf")
                .nin("12345678901")
                .birthday(LocalDate.of(1995, 6, 15))
                .gender("Female")
                .addressLine("Allen Avenue, Ikeja")
                .build();

        ProfileCompleteness result = calculator.calculate(user, profile);

        assertThat(result.getPercentComplete()).isEqualTo(100);
        assertThat(result.getSteps()).allMatch(ProfileStep::isComplete);
    }

    @Test
    void calculate_returnsPartialCompleteness_whenOnlyEmailVerified() {
        Users user = Users.builder()
                .email("seeker@example.com")
                .role(UserRole.JOB_SEEKER)
                .status(UserStatus.PENDING)
                .emailVerified(true)
                .build();
        JobSeeker profile = JobSeeker.builder()
                .user(user)
                .status(UserStatus.PENDING)
                .phone("+2348012345678")
                .build();

        ProfileCompleteness result = calculator.calculate(user, profile);

        assertThat(result.getPercentComplete()).isEqualTo(25);
        assertThat(result.getSteps()).filteredOn(ProfileStep::isComplete)
                .extracting(ProfileStep::getKey)
                .containsExactly("email_verified");
    }
}
