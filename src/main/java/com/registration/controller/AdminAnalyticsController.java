package com.registration.controller;

import com.registration.dto.BestSellingProductResponse;
import com.registration.dto.RevenueResponse;
import com.registration.service.AnalyticsService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/analytics")
@PreAuthorize("hasRole('ADMIN')")
public class AdminAnalyticsController {

    private final AnalyticsService analyticsService;

    public AdminAnalyticsController(AnalyticsService analyticsService) {
        this.analyticsService = analyticsService;
    }

    @GetMapping("/daily")
    public ResponseEntity<RevenueResponse> dailyRevenue(@RequestParam("date") String date) {
        return ResponseEntity.ok(analyticsService.dailyRevenue(date));
    }

    @GetMapping("/monthly")
    public ResponseEntity<RevenueResponse> monthlyRevenue(
            @RequestParam("year") Integer year,
            @RequestParam("month") Integer month) {
        return ResponseEntity.ok(analyticsService.monthlyRevenue(year, month));
    }

    @GetMapping("/yearly")
    public ResponseEntity<RevenueResponse> yearlyRevenue(@RequestParam("year") Integer year) {
        return ResponseEntity.ok(analyticsService.yearlyRevenue(year));
    }

    @GetMapping("/overall")
    public ResponseEntity<RevenueResponse> overallRevenue() {
        return ResponseEntity.ok(analyticsService.overallRevenue());
    }

    @GetMapping("/best-selling")
    public ResponseEntity<List<BestSellingProductResponse>> bestSellingProducts() {
        return ResponseEntity.ok(analyticsService.getBestSellingProducts());
    }
}