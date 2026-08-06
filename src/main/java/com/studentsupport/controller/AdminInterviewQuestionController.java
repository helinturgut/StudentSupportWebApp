package com.studentsupport.controller;

import com.studentsupport.dto.InterviewQuestionRequest;
import com.studentsupport.dto.InterviewQuestionResponse;
import com.studentsupport.service.InterviewQuestionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/interview-questions")
@RequiredArgsConstructor
public class AdminInterviewQuestionController {

    private final InterviewQuestionService interviewQuestionService;

    @PostMapping
    public ResponseEntity<InterviewQuestionResponse> create(@Valid @RequestBody InterviewQuestionRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(interviewQuestionService.create(request));
    }

    @PutMapping("/{questionId}")
    public InterviewQuestionResponse update(@PathVariable Long questionId,
                                             @Valid @RequestBody InterviewQuestionRequest request) {
        return interviewQuestionService.update(questionId, request);
    }

    @DeleteMapping("/{questionId}")
    public ResponseEntity<Void> delete(@PathVariable Long questionId) {
        interviewQuestionService.delete(questionId);
        return ResponseEntity.noContent().build();
    }
}
