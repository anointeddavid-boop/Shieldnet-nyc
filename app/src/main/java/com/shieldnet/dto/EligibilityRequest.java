
package com.shieldnet.dto;

import com.shieldnet.model.Borough;
import com.shieldnet.model.BenefitType;
import jakarta.validation.constraints.*;
import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EligibilityRequest {

    @NotBlank(message = "First name is required")
    @Size(max = 100)
    private String firstName;

    @NotBlank(message = "Last name is required")
    @Size(max = 100)
    private String lastName;

    @NotNull(message = "Household size is required")
    @Min(value = 1, message = "Household size must be at least 1")
    @Max(value = 20, message = "Household size cannot exceed 20")
    private Integer householdSize;

    @NotNull(message = "Monthly income is required")
    @DecimalMin(value = "0.0", message = "Monthly income cannot be negative")
    private Double monthlyIncome;

    @NotNull(message = "Borough is required")
    private Borough borough;

    @NotNull(message = "Benefit type is required")
    private BenefitType benefitType;

    private String caseId;
    private boolean emergencyApplication;
}
