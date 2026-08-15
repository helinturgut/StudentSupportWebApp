package com.studentsupport.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CvFeedbackRequest {

    @NotBlank(message = "CV text is required")
    private String inputText;
}
