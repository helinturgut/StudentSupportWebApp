package com.studentsupport.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class CvFeedbackResponse {

    private Long cvFeedbackId;
    private String inputText;
    private String feedbackText;
    private LocalDateTime createdAt;
}
