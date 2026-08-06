package com.studentsupport.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class AdminAnalyticsResponse {

    private long userCount;
    private long resourceCount;
    private long chatSessionCount;
    private long careerPathwayCount;
    private long interviewQuestionCount;
    private long feedbackCount;
    private Double averageFeedbackRating;
}
