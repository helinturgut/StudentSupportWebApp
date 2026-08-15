package com.studentsupport.service;

import com.studentsupport.dto.CvFeedbackResponse;
import com.studentsupport.entity.CvFeedback;
import com.studentsupport.entity.User;
import com.studentsupport.exception.ResourceNotFoundException;
import com.studentsupport.repository.CvFeedbackRepository;
import com.studentsupport.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class CvFeedbackService {

    private final CvFeedbackRepository cvFeedbackRepository;
    private final UserRepository userRepository;
    private final GeminiService geminiService;
    private final CvFileParsingService cvFileParsingService;

    public CvFeedbackResponse submit(Long userId, String inputText) {
        User user = getUser(userId);

        GeminiService.AiResult result = geminiService.generateCvFeedback(inputText);

        CvFeedback cvFeedback = CvFeedback.builder()
                .user(user)
                .inputText(inputText)
                .feedbackText(result.text())
                .build();

        return toResponse(cvFeedbackRepository.save(cvFeedback));
    }

    public CvFeedbackResponse submitFromFile(Long userId, MultipartFile file) {
        String extractedText = cvFileParsingService.extractText(file);
        return submit(userId, extractedText);
    }

    public List<CvFeedbackResponse> listForUser(Long userId) {
        User user = getUser(userId);
        return cvFeedbackRepository.findByUserOrderByCreatedAtDesc(user).stream()
                .map(this::toResponse)
                .toList();
    }

    public CvFeedbackResponse getById(Long userId, Long cvFeedbackId) {
        User user = getUser(userId);
        return cvFeedbackRepository.findByCvFeedbackIdAndUser(cvFeedbackId, user)
                .map(this::toResponse)
                .orElseThrow(() -> new ResourceNotFoundException("CV feedback record not found"));
    }

    private User getUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }

    private CvFeedbackResponse toResponse(CvFeedback cvFeedback) {
        return CvFeedbackResponse.builder()
                .cvFeedbackId(cvFeedback.getCvFeedbackId())
                .inputText(cvFeedback.getInputText())
                .feedbackText(cvFeedback.getFeedbackText())
                .createdAt(cvFeedback.getCreatedAt())
                .build();
    }
}
