package com.studentsupport.controller;

import com.studentsupport.dto.CvFeedbackRequest;
import com.studentsupport.dto.CvFeedbackResponse;
import com.studentsupport.security.AuthenticatedUser;
import com.studentsupport.service.CvFeedbackService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/cv-feedback")
@RequiredArgsConstructor
public class CvFeedbackController {

    private final CvFeedbackService cvFeedbackService;

    @GetMapping
    public List<CvFeedbackResponse> list(@AuthenticationPrincipal AuthenticatedUser principal) {
        return cvFeedbackService.listForUser(principal.userId());
    }

    @GetMapping("/{cvFeedbackId}")
    public CvFeedbackResponse getById(@AuthenticationPrincipal AuthenticatedUser principal,
                                       @PathVariable Long cvFeedbackId) {
        return cvFeedbackService.getById(principal.userId(), cvFeedbackId);
    }

    @PostMapping
    public ResponseEntity<CvFeedbackResponse> submit(@AuthenticationPrincipal AuthenticatedUser principal,
                                                       @Valid @RequestBody CvFeedbackRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(cvFeedbackService.submit(principal.userId(), request.getInputText(), request.isDetailed()));
    }

    @PostMapping(value = "/upload", consumes = "multipart/form-data")
    public ResponseEntity<CvFeedbackResponse> submitFile(@AuthenticationPrincipal AuthenticatedUser principal,
                                                           @RequestParam("file") MultipartFile file,
                                                           @RequestParam(value = "detailed", defaultValue = "false") boolean detailed) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(cvFeedbackService.submitFromFile(principal.userId(), file, detailed));
    }

    @DeleteMapping("/{cvFeedbackId}")
    public ResponseEntity<Void> delete(@AuthenticationPrincipal AuthenticatedUser principal,
                                        @PathVariable Long cvFeedbackId) {
        cvFeedbackService.delete(principal.userId(), cvFeedbackId);
        return ResponseEntity.noContent().build();
    }
}
