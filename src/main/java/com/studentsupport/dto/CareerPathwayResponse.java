package com.studentsupport.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
public class CareerPathwayResponse {

    private Long pathwayId;
    private String profession;
    private String title;
    private String description;
    private String requiredSkills;
    private Long createdByUserId;
    private String createdByName;
    private LocalDateTime createdAt;
    private List<PathwayResourceResponse> resources;
}
