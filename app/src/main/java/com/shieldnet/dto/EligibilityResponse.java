package com.shieldnet.dto;

import com.shieldnet.model.BenefitType;
import com.shieldnet.model.EligibilityStatus;
import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EligibilityResponse {

    private String applicationId;
    private EligibilityStatus status;
    private BenefitType benefitType;
    private String determinationReason;
    private Double estimatedMonthlyBenefit;
    private List<String> nextSteps;
    private List<String> additionalProgramReferrals;
    private LocalDateTime processedAt;
    private String processingNodeId;
}
