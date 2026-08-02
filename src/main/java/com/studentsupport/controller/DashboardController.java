package com.studentsupport.controller;

import com.studentsupport.dto.DashboardResponse;
import com.studentsupport.security.AuthenticatedUser;
import com.studentsupport.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/students/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;

    @GetMapping
    public DashboardResponse getDashboard(@AuthenticationPrincipal AuthenticatedUser principal) {
        return dashboardService.getDashboard(principal.userId());
    }
}
