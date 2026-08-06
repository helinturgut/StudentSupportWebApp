package com.studentsupport.service;

import com.studentsupport.dto.InterviewQuestionOptionRequest;
import com.studentsupport.dto.InterviewQuestionOptionResponse;
import com.studentsupport.dto.InterviewQuestionRequest;
import com.studentsupport.dto.InterviewQuestionResponse;
import com.studentsupport.entity.InterviewQuestion;
import com.studentsupport.entity.InterviewQuestionOption;
import com.studentsupport.exception.BadRequestException;
import com.studentsupport.exception.ResourceNotFoundException;
import com.studentsupport.repository.InterviewQuestionOptionRepository;
import com.studentsupport.repository.InterviewQuestionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class InterviewQuestionService {

    private final InterviewQuestionRepository interviewQuestionRepository;
    private final InterviewQuestionOptionRepository interviewQuestionOptionRepository;

    public Page<InterviewQuestionResponse> search(String category, com.studentsupport.entity.Difficulty difficulty, Pageable pageable) {
        String normalizedCategory = StringUtils.hasText(category) ? category : null;
        return interviewQuestionRepository.findByCategoryAndDifficulty(normalizedCategory, difficulty, pageable)
                .map(this::toResponse);
    }

    public List<String> getCategories() {
        return interviewQuestionRepository.findDistinctCategories();
    }

    public InterviewQuestionResponse getById(Long questionId) {
        return toResponse(findById(questionId));
    }

    public InterviewQuestionResponse create(InterviewQuestionRequest request) {
        InterviewQuestion question = InterviewQuestion.builder()
                .category(request.getCategory())
                .questionText(request.getQuestionText())
                .guidanceText(request.getGuidanceText())
                .difficulty(request.getDifficulty())
                .build();
        question = interviewQuestionRepository.save(question);
        saveOptions(question, request.getOptions());
        return toResponse(question);
    }

    public InterviewQuestionResponse update(Long questionId, InterviewQuestionRequest request) {
        InterviewQuestion question = findById(questionId);
        question.setCategory(request.getCategory());
        question.setQuestionText(request.getQuestionText());
        question.setGuidanceText(request.getGuidanceText());
        question.setDifficulty(request.getDifficulty());
        question = interviewQuestionRepository.save(question);

        interviewQuestionOptionRepository.deleteByQuestion(question);
        saveOptions(question, request.getOptions());
        return toResponse(question);
    }

    public void delete(Long questionId) {
        InterviewQuestion question = findById(questionId);
        interviewQuestionOptionRepository.deleteByQuestion(question);
        interviewQuestionRepository.deleteById(questionId);
    }

    private void saveOptions(InterviewQuestion question, List<InterviewQuestionOptionRequest> optionRequests) {
        if (optionRequests == null || optionRequests.isEmpty()) {
            return;
        }
        if (optionRequests.stream().noneMatch(InterviewQuestionOptionRequest::isCorrect)) {
            throw new BadRequestException("At least one option must be marked as correct");
        }
        for (InterviewQuestionOptionRequest optionRequest : optionRequests) {
            interviewQuestionOptionRepository.save(InterviewQuestionOption.builder()
                    .question(question)
                    .optionText(optionRequest.getOptionText())
                    .correct(optionRequest.isCorrect())
                    .build());
        }
    }

    private InterviewQuestion findById(Long questionId) {
        return interviewQuestionRepository.findById(questionId)
                .orElseThrow(() -> new ResourceNotFoundException("Interview question not found"));
    }

    private InterviewQuestionResponse toResponse(InterviewQuestion question) {
        List<InterviewQuestionOptionResponse> options = interviewQuestionOptionRepository
                .findByQuestionOrderByOptionIdAsc(question).stream()
                .map(o -> InterviewQuestionOptionResponse.builder()
                        .optionId(o.getOptionId())
                        .optionText(o.getOptionText())
                        .correct(o.isCorrect())
                        .build())
                .toList();

        return InterviewQuestionResponse.builder()
                .questionId(question.getQuestionId())
                .category(question.getCategory())
                .questionText(question.getQuestionText())
                .guidanceText(question.getGuidanceText())
                .difficulty(question.getDifficulty())
                .options(options)
                .build();
    }
}
