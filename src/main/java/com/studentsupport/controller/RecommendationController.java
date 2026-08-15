package com.studentsupport.controller;

import com.studentsupport.dto.RecommendationResponse;
import com.studentsupport.security.AuthenticatedUser;
import com.studentsupport.service.RecommendationService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/recommendations")
@RequiredArgsConstructor
public class RecommendationController {

    private final RecommendationService recommendationService;

    @GetMapping
    public List<RecommendationResponse> get(@AuthenticationPrincipal AuthenticatedUser principal) {
        return recommendationService.getForUser(principal.userId());
    }

    @PostMapping("/refresh")
    public List<RecommendationResponse> refresh(@AuthenticationPrincipal AuthenticatedUser principal) {
        return recommendationService.generateForUser(principal.userId());
    }
}
