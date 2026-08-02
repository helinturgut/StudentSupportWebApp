package com.studentsupport.security;

import com.studentsupport.entity.RoleName;

public record AuthenticatedUser(Long userId, String email, RoleName role) {
}
