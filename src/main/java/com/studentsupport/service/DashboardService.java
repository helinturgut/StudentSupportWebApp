package com.studentsupport.service;

import com.studentsupport.dto.DashboardResponse;
import com.studentsupport.entity.StudentProfile;
import com.studentsupport.entity.User;
import com.studentsupport.exception.ResourceNotFoundException;
import com.studentsupport.repository.ChatSessionRepository;
import com.studentsupport.repository.CvFeedbackRepository;
import com.studentsupport.repository.InterviewQuestionRepository;
import com.studentsupport.repository.ResourceRepository;
import com.studentsupport.repository.StudentProfileRepository;
import com.studentsupport.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class DashboardService {

    private final UserRepository userRepository;
    private final StudentProfileRepository studentProfileRepository;
    private final ChatSessionRepository chatSessionRepository;
    private final CvFeedbackRepository cvFeedbackRepository;
    private final ResourceRepository resourceRepository;
    private final InterviewQuestionRepository interviewQuestionRepository;

    public DashboardResponse getDashboard(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        StudentProfile profile = studentProfileRepository.findByUserUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Student profile not found"));

        return DashboardResponse.builder()
                .fullName(user.getFullName())
                .email(user.getEmail())
                .course(profile.getCourse())
                .careerGoal(profile.getCareerGoal())
                .chatSessionCount(chatSessionRepository.countByUser(user))
                .cvFeedbackCount(cvFeedbackRepository.countByUser(user))
                .availableResourceCount(resourceRepository.count())
                .availableInterviewQuestionCount(interviewQuestionRepository.count())
                .build();
    }
}
