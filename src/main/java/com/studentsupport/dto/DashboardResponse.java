package com.studentsupport.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class DashboardResponse {

    private String fullName;
    private String email;
    private String course;
    private String careerGoal;
    private long chatSessionCount;
    private long cvFeedbackCount;
    private long availableResourceCount;
    private long availableInterviewQuestionCount;
}
