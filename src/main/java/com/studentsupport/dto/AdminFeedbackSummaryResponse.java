package com.studentsupport.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class AdminFeedbackSummaryResponse {

    private Double averageRating;
    private long totalFeedbackCount;
    private List<UserFeedbackResponse> feedback;
}
