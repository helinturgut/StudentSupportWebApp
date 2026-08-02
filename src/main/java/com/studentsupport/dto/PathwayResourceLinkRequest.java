package com.studentsupport.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PathwayResourceLinkRequest {

    @NotNull(message = "Resource id is required")
    private Long resourceId;

    private String linkReason;
}
