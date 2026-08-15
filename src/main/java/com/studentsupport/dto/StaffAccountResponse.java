package com.studentsupport.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class StaffAccountResponse {

    private Long userId;
    private String fullName;
    private String email;
    private String status;
    private LocalDateTime createdAt;
}
