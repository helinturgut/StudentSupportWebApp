package com.studentsupport.service;

import com.studentsupport.dto.AdminFeedbackSummaryResponse;
import com.studentsupport.dto.UserFeedbackRequest;
import com.studentsupport.dto.UserFeedbackResponse;
import com.studentsupport.entity.User;
import com.studentsupport.entity.UserFeedback;
import com.studentsupport.exception.ResourceNotFoundException;
import com.studentsupport.repository.UserFeedbackRepository;
import com.studentsupport.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class UserFeedbackService {

    private final UserFeedbackRepository userFeedbackRepository;
    private final UserRepository userRepository;

    public UserFeedbackResponse submit(Long userId, UserFeedbackRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        UserFeedback feedback = UserFeedback.builder()
                .user(user)
                .rating(request.getRating())
                .comment(request.getComment())
                .build();

        return toResponse(userFeedbackRepository.save(feedback));
    }

    public List<UserFeedbackResponse> listOwn(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        return userFeedbackRepository.findByUserOrderByCreatedAtDesc(user).stream()
                .map(this::toResponse)
                .toList();
    }

    public AdminFeedbackSummaryResponse listAll() {
        List<UserFeedbackResponse> feedback = userFeedbackRepository.findAll().stream()
                .map(this::toResponse)
                .toList();

        return AdminFeedbackSummaryResponse.builder()
                .averageRating(userFeedbackRepository.findAverageRating())
                .totalFeedbackCount(userFeedbackRepository.count())
                .feedback(feedback)
                .build();
    }

    private UserFeedbackResponse toResponse(UserFeedback feedback) {
        return UserFeedbackResponse.builder()
                .feedbackId(feedback.getFeedbackId())
                .userId(feedback.getUser().getUserId())
                .userFullName(feedback.getUser().getFullName())
                .rating(feedback.getRating())
                .comment(feedback.getComment())
                .createdAt(feedback.getCreatedAt())
                .build();
    }
}
