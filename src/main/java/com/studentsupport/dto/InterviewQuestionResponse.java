package com.studentsupport.dto;

import com.studentsupport.entity.Difficulty;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class InterviewQuestionResponse {

    private Long questionId;
    private String category;
    private String questionText;
    private String guidanceText;
    private Difficulty difficulty;
    private List<InterviewQuestionOptionResponse> options;
}
