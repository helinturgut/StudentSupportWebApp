package com.studentsupport.service;

import com.studentsupport.dto.AdminAnalyticsResponse;
import com.studentsupport.repository.CareerPathwayRepository;
import com.studentsupport.repository.ChatSessionRepository;
import com.studentsupport.repository.InterviewQuestionRepository;
import com.studentsupport.repository.ResourceRepository;
import com.studentsupport.repository.UserFeedbackRepository;
import com.studentsupport.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AdminAnalyticsService {

    private final UserRepository userRepository;
    private final ResourceRepository resourceRepository;
    private final ChatSessionRepository chatSessionRepository;
    private final CareerPathwayRepository careerPathwayRepository;
    private final InterviewQuestionRepository interviewQuestionRepository;
    private final UserFeedbackRepository userFeedbackRepository;

    public AdminAnalyticsResponse getAnalytics() {
        return AdminAnalyticsResponse.builder()
                .userCount(userRepository.count())
                .resourceCount(resourceRepository.count())
                .chatSessionCount(chatSessionRepository.count())
                .careerPathwayCount(careerPathwayRepository.count())
                .interviewQuestionCount(interviewQuestionRepository.count())
                .feedbackCount(userFeedbackRepository.count())
                .averageFeedbackRating(userFeedbackRepository.findAverageRating())
                .build();
    }
}
