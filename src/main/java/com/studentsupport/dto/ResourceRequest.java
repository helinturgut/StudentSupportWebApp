package com.studentsupport.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ResourceRequest {

    @NotBlank(message = "Title is required")
    private String title;

    private String description;

    private String category;

    private String url;
}
