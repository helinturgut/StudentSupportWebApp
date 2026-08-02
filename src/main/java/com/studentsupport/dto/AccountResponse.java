package com.studentsupport.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class AccountResponse {

    private Long userId;
    private String fullName;
    private String email;
}
