package com.studentsupport.dto;

import com.studentsupport.entity.Difficulty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class InterviewQuestionRequest {

    @NotBlank(message = "Category is required")
    private String category;

    @NotBlank(message = "Question text is required")
    private String questionText;

    private String guidanceText;

    @NotNull(message = "Difficulty is required")
    private Difficulty difficulty;

    private List<InterviewQuestionOptionRequest> options;
}
