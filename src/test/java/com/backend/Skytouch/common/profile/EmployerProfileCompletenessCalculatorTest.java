package com.backend.Skytouch.common.profile;

import com.backend.Skytouch.common.enums.UserRole;
import com.backend.Skytouch.common.enums.UserStatus;
import com.backend.Skytouch.employer.entity.Employer;
import com.backend.Skytouch.user.entity.Users;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class EmployerProfileCompletenessCalculatorTest {

    private final EmployerProfileCompletenessCalculator calculator =
            new EmployerProfileCompletenessCalculator();

    @Test
    void calculate_marksCompanyLinkedIncomplete_untilPhase3() {
        Users user = Users.builder()
                .email("hr@acme.com")
                .role(UserRole.EMPLOYER)
                .status(UserStatus.ACTIVE)
                .emailVerified(true)
                .build();
        Employer profile = Employer.builder()
                .user(user)
                .status(UserStatus.ACTIVE)
                .firstName("Jane")
                .lastName("Doe")
                .phone("+2348012345678")
                .companyName("Acme Ltd")
                .jobTitle("HR Manager")
                .build();

        ProfileCompleteness result = calculator.calculate(user, profile, false);

        assertThat(result.getPercentComplete()).isEqualTo(80);
        assertThat(result.getSteps()).filteredOn(step -> !step.isComplete())
                .extracting(ProfileStep::getKey)
                .containsExactly("company_linked");
    }
}
