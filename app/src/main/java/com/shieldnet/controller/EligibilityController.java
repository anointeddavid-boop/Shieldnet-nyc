package com.shieldnet.controller;

import com.shieldnet.dto.EligibilityRequest;
import com.shieldnet.dto.EligibilityResponse;
import com.shieldnet.service.EligibilityService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1")
public class EligibilityController {

    private final EligibilityService eligibilityService;

    public EligibilityController(EligibilityService eligibilityService) {
        this.eligibilityService = eligibilityService;
    }

    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> health() {
        return ResponseEntity.ok(Map.of(
            "status", "UP",
            "service", "ShieldNet Benefits Eligibility API",
            "version", "1.0.0"
        ));
    }

    @PostMapping("/eligibility")
    public ResponseEntity<EligibilityResponse> checkEligibility(
            @Valid @RequestBody EligibilityRequest request) {
        EligibilityResponse response = eligibilityService.checkEligibility(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/eligibility/batch")
    public ResponseEntity<?> batchCheck(
            @Valid @RequestBody java.util.List<EligibilityRequest> requests) {
        var results = requests.stream()
            .map(eligibilityService::checkEligibility)
            .toList();
        return ResponseEntity.ok(Map.of(
            "totalProcessed", results.size(),
            "results", results
        ));
    }
}
