package com.studentsupport.controller;

import com.studentsupport.dto.StudentProfileRequest;
import com.studentsupport.dto.StudentProfileResponse;
import com.studentsupport.security.AuthenticatedUser;
import com.studentsupport.service.StudentProfileService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/students/profile")
@RequiredArgsConstructor
public class StudentProfileController {

    private final StudentProfileService studentProfileService;

    @GetMapping
    public StudentProfileResponse getMyProfile(@AuthenticationPrincipal AuthenticatedUser principal) {
        return studentProfileService.getProfile(principal.userId());
    }

    @PutMapping
    public StudentProfileResponse updateMyProfile(@AuthenticationPrincipal AuthenticatedUser principal,
                                                   @Valid @RequestBody StudentProfileRequest request) {
        return studentProfileService.updateProfile(principal.userId(), request);
    }
}
