package com.studentsupport.controller;

import com.studentsupport.dto.InterviewQuestionResponse;
import com.studentsupport.entity.Difficulty;
import com.studentsupport.service.InterviewQuestionService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/interview-questions")
@RequiredArgsConstructor
public class InterviewQuestionController {

    private final InterviewQuestionService interviewQuestionService;

    @GetMapping
    public Page<InterviewQuestionResponse> search(@RequestParam(required = false) String category,
                                                   @RequestParam(required = false) Difficulty difficulty,
                                                   Pageable pageable) {
        return interviewQuestionService.search(category, difficulty, pageable);
    }

    @GetMapping("/categories")
    public List<String> getCategories() {
        return interviewQuestionService.getCategories();
    }

    @GetMapping("/{questionId}")
    public InterviewQuestionResponse getById(@PathVariable Long questionId) {
        return interviewQuestionService.getById(questionId);
    }
}
