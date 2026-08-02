package com.studentsupport.service;

import com.studentsupport.dto.StudentProfileRequest;
import com.studentsupport.dto.StudentProfileResponse;
import com.studentsupport.entity.StudentProfile;
import com.studentsupport.exception.ResourceNotFoundException;
import com.studentsupport.repository.StudentProfileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class StudentProfileService {

    private final StudentProfileRepository studentProfileRepository;

    public StudentProfileResponse getProfile(Long userId) {
        return toResponse(findByUserId(userId));
    }

    public StudentProfileResponse updateProfile(Long userId, StudentProfileRequest request) {
        StudentProfile profile = findByUserId(userId);
        profile.setCourse(request.getCourse());
        profile.setPreviousBackground(request.getPreviousBackground());
        profile.setCareerGoal(request.getCareerGoal());
        profile.setSkillInterests(request.getSkillInterests());
        studentProfileRepository.save(profile);
        return toResponse(profile);
    }

    private StudentProfile findByUserId(Long userId) {
        return studentProfileRepository.findByUserUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Student profile not found"));
    }

    private StudentProfileResponse toResponse(StudentProfile profile) {
        return StudentProfileResponse.builder()
                .profileId(profile.getProfileId())
                .userId(profile.getUser().getUserId())
                .course(profile.getCourse())
                .previousBackground(profile.getPreviousBackground())
                .careerGoal(profile.getCareerGoal())
                .skillInterests(profile.getSkillInterests())
                .build();
    }
}
