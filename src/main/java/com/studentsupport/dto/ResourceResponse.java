package com.studentsupport.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class ResourceResponse {

    private Long resourceId;
    private String title;
    private String description;
    private String category;
    private String url;
    private Long createdByUserId;
    private String createdByName;
    private LocalDateTime createdAt;
}
