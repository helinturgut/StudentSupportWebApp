package com.studentsupport.dto;

import com.studentsupport.entity.RecommendationType;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class RecommendationResponse {

    private Long recommendationId;
    private RecommendationType recommendationType;
    private Long resourceId;
    private String resourceTitle;
    private Long pathwayId;
    private String pathwayTitle;
    private String reason;
    private LocalDateTime createdAt;
}
