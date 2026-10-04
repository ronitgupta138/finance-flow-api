package com.financeflow.controller;

import com.financeflow.dto.ApiResponse;
import com.financeflow.dto.MonthlySummaryResponse;
import com.financeflow.security.UserPrincipal;
import com.financeflow.service.AnalyticsService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/analytics")
public class AnalyticsController {

    private final AnalyticsService analyticsService;

    public AnalyticsController(AnalyticsService analyticsService) {
        this.analyticsService = analyticsService;
    }

    @GetMapping("/monthly-summary")
    public ResponseEntity<ApiResponse<MonthlySummaryResponse>> getMonthlySummary(
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) Integer month,
            @AuthenticationPrincipal UserPrincipal principal) {
        LocalDate now = LocalDate.now();
        int targetYear = (year != null) ? year : now.getYear();
        int targetMonth = (month != null) ? month : now.getMonthValue();

        MonthlySummaryResponse summary = analyticsService.getMonthlySummary(targetYear, targetMonth, principal);
        return ResponseEntity.ok(ApiResponse.ok(summary));
    }
}
