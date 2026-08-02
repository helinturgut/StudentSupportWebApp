package com.studentsupport.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class AuthResponse {

    private String message;
    private Long userId;
    private String email;
    private String fullName;
    private String role;
    private String token;
    private String refreshToken;
}
