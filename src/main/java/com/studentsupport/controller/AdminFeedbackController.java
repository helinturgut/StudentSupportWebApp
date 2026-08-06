package com.studentsupport.controller;

import com.studentsupport.dto.AdminFeedbackSummaryResponse;
import com.studentsupport.service.UserFeedbackService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/feedback")
@RequiredArgsConstructor
public class AdminFeedbackController {

    private final UserFeedbackService userFeedbackService;

    @GetMapping
    public AdminFeedbackSummaryResponse listAll() {
        return userFeedbackService.listAll();
    }
}
