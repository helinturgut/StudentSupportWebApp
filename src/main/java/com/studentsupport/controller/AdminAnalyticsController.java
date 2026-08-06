package com.studentsupport.controller;

import com.studentsupport.dto.AdminAnalyticsResponse;
import com.studentsupport.service.AdminAnalyticsService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/analytics")
@RequiredArgsConstructor
public class AdminAnalyticsController {

    private final AdminAnalyticsService adminAnalyticsService;

    @GetMapping
    public AdminAnalyticsResponse getAnalytics() {
        return adminAnalyticsService.getAnalytics();
    }
}
