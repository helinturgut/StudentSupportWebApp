package com.studentsupport.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CareerPathwayRequest {

    @NotBlank(message = "Profession is required")
    private String profession;

    @NotBlank(message = "Title is required")
    private String title;

    private String description;

    private String requiredSkills;
}
