package com.studentsupport.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class InterviewQuestionOptionResponse {

    private Long optionId;
    private String optionText;
    private boolean correct;
}
