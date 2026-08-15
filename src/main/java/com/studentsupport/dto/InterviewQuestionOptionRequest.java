package com.studentsupport.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class InterviewQuestionOptionRequest {

    @NotBlank(message = "Option text is required")
    private String optionText;

    private boolean correct;
}
