
package com.shieldnet.service;

import com.shieldnet.dto.EligibilityRequest;
import com.shieldnet.dto.EligibilityResponse;
import com.shieldnet.model.EligibilityStatus;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class EligibilityService {

    private static final double[] FPL_MONTHLY = {
        0, 1215, 1644, 2072, 2500, 2929, 3357, 3786, 4214
    };

    public EligibilityResponse checkEligibility(EligibilityRequest request) {
        return switch (request.getBenefitType()) {
            case SNAP -> checkSNAP(request);
            case EMERGENCY_RENTAL_ASSISTANCE -> checkERA(request);
            case DHS_SHELTER -> checkDHSShelter(request);
            case CASH_ASSISTANCE -> checkCashAssistance(request);
            case MEDICAID -> checkMedicaid(request);
        };
    }

    private EligibilityResponse checkSNAP(EligibilityRequest req) {
        int size = Math.min(req.getHouseholdSize(), FPL_MONTHLY.length - 1);
        double incomeLimit = FPL_MONTHLY[size] * 1.30;
        boolean eligible = req.getMonthlyIncome() <= incomeLimit;

        if (req.isEmergencyApplication() && !eligible && req.getMonthlyIncome() <= incomeLimit * 1.5) {
            return buildResponse(req, EligibilityStatus.EMERGENCY_APPROVED,
                "Emergency SNAP approved under disaster-period expanded eligibility.",
                estimateSNAPBenefit(req), snapNextSteps(), List.of("CASH_ASSISTANCE", "MEDICAID"));
        }

        return buildResponse(req,
            eligible ? EligibilityStatus.APPROVED : EligibilityStatus.DENIED,
            eligible
                ? "Household income is within SNAP eligibility limits (130% FPL)."
                : String.format("Household income $%.2f exceeds SNAP limit of $%.2f/month.",
                    req.getMonthlyIncome(), incomeLimit),
            eligible ? estimateSNAPBenefit(req) : 0.0,
            snapNextSteps(),
            eligible ? List.of("MEDICAID", "CASH_ASSISTANCE") : List.of()
        );
    }

    private EligibilityResponse checkERA(EligibilityRequest req) {
        double incomeLimit = FPL_MONTHLY[Math.min(req.getHouseholdSize(), FPL_MONTHLY.length - 1)] * 0.80;
        boolean eligible = req.getMonthlyIncome() <= incomeLimit;

        return buildResponse(req,
            eligible ? EligibilityStatus.APPROVED : EligibilityStatus.PENDING_DOCUMENTATION,
            eligible
                ? "Applicant qualifies for Emergency Rental Assistance. Landlord verification required."
                : "Income above threshold. Caseworker review required for ERA determination.",
            eligible ? 1500.0 : 0.0,
            List.of("Contact your landlord to submit W-9", "Upload proof of lease", "Attend intake appointment"),
            List.of("SNAP", "CASH_ASSISTANCE")
        );
    }

    private EligibilityResponse checkDHSShelter(EligibilityRequest req) {
        return buildResponse(req,
            EligibilityStatus.REFERRED_TO_CASEWORKER,
            "DHS shelter placement requires in-person intake at a PATH Center.",
            0.0,
            List.of(
                "Visit the nearest PATH Center (Prevention Assistance and Temporary Housing)",
                "Bronx: 151 E 151st St | Manhattan: 400 8th Ave | Brooklyn: 275 Atlantic Ave",
                "Bring ID and any documentation of housing situation"
            ),
            List.of("SNAP", "CASH_ASSISTANCE", "MEDICAID")
        );
    }

    private EligibilityResponse checkCashAssistance(EligibilityRequest req) {
        double incomeLimit = FPL_MONTHLY[Math.min(req.getHouseholdSize(), FPL_MONTHLY.length - 1)] * 0.72;
        boolean eligible = req.getMonthlyIncome() <= incomeLimit;

        return buildResponse(req,
            eligible ? EligibilityStatus.APPROVED : EligibilityStatus.DENIED,
            eligible ? "Applicant qualifies for Cash Assistance." : "Income exceeds Cash Assistance limits.",
            eligible ? 789.0 : 0.0,
            List.of("Complete work activity requirements", "Report monthly income changes", "Attend biannual recertification"),
            eligible ? List.of("SNAP", "MEDICAID") : List.of()
        );
    }

    private EligibilityResponse checkMedicaid(EligibilityRequest req) {
        double incomeLimit = FPL_MONTHLY[Math.min(req.getHouseholdSize(), FPL_MONTHLY.length - 1)] * 1.38;
        boolean eligible = req.getMonthlyIncome() <= incomeLimit;

        return buildResponse(req,
            eligible ? EligibilityStatus.APPROVED : EligibilityStatus.DENIED,
            eligible ? "Applicant qualifies for Medicaid." : "Income exceeds Medicaid threshold. Consider NY State of Health marketplace.",
            0.0,
            eligible
                ? List.of("Medicaid card will be mailed within 10 business days", "Choose a health plan at nystateofhealth.ny.gov")
                : List.of("Visit nystateofhealth.ny.gov for marketplace options"),
            eligible ? List.of("SNAP", "CASH_ASSISTANCE") : List.of()
        );
    }

    private double estimateSNAPBenefit(EligibilityRequest req) {
        double maxAllotment = switch (Math.min(req.getHouseholdSize(), 8)) {
            case 1 -> 291.0;
            case 2 -> 535.0;
            case 3 -> 766.0;
            case 4 -> 973.0;
            case 5 -> 1155.0;
            case 6 -> 1386.0;
            case 7 -> 1532.0;
            default -> 1751.0;
        };
        double netIncome = req.getMonthlyIncome() * 0.70;
        return Math.max(0, maxAllotment - (netIncome * 0.30));
    }

    private List<String> snapNextSteps() {
        return List.of(
            "Visit your local HRA SNAP Center or apply online at access.nyc.gov",
            "Bring proof of identity, income, and residency",
            "Determination issued within 30 days (7 days if expedited)"
        );
    }

    private EligibilityResponse buildResponse(EligibilityRequest req, EligibilityStatus status,
                                               String reason, double benefit,
                                               List<String> nextSteps, List<String> referrals) {
        return EligibilityResponse.builder()
            .applicationId("SN-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase())
            .status(status)
            .benefitType(req.getBenefitType())
            .determinationReason(reason)
            .estimatedMonthlyBenefit(benefit)
            .nextSteps(nextSteps)
            .additionalProgramReferrals(referrals)
            .processedAt(LocalDateTime.now())
            .processingNodeId(System.getenv().getOrDefault("HOSTNAME", "local-dev"))
            .build();
    }
}
