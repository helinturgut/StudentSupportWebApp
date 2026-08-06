package com.studentsupport.controller;

import com.studentsupport.dto.UserFeedbackRequest;
import com.studentsupport.dto.UserFeedbackResponse;
import com.studentsupport.security.AuthenticatedUser;
import com.studentsupport.service.UserFeedbackService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/feedback")
@RequiredArgsConstructor
public class FeedbackController {

    private final UserFeedbackService userFeedbackService;

    @PostMapping
    public ResponseEntity<UserFeedbackResponse> submit(@AuthenticationPrincipal AuthenticatedUser principal,
                                                         @Valid @RequestBody UserFeedbackRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(userFeedbackService.submit(principal.userId(), request));
    }

    @GetMapping
    public List<UserFeedbackResponse> listOwn(@AuthenticationPrincipal AuthenticatedUser principal) {
        return userFeedbackService.listOwn(principal.userId());
    }
}
