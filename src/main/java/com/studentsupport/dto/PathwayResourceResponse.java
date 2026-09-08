package com.studentsupport.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class PathwayResourceResponse {

    private Long resourceId;
    private String resourceTitle;
    private String linkReason;
}
