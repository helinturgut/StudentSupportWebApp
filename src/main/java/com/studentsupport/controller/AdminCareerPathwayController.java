package com.studentsupport.controller;

import com.studentsupport.dto.CareerPathwayRequest;
import com.studentsupport.dto.CareerPathwayResponse;
import com.studentsupport.dto.PathwayResourceLinkRequest;
import com.studentsupport.security.AuthenticatedUser;
import com.studentsupport.service.CareerPathwayService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/career-pathways")
@RequiredArgsConstructor
public class AdminCareerPathwayController {

    private final CareerPathwayService careerPathwayService;

    @PostMapping
    public ResponseEntity<CareerPathwayResponse> create(@AuthenticationPrincipal AuthenticatedUser principal,
                                                          @Valid @RequestBody CareerPathwayRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(careerPathwayService.create(principal.userId(), request));
    }

    @PutMapping("/{pathwayId}")
    public CareerPathwayResponse update(@PathVariable Long pathwayId,
                                         @Valid @RequestBody CareerPathwayRequest request) {
        return careerPathwayService.update(pathwayId, request);
    }

    @DeleteMapping("/{pathwayId}")
    public ResponseEntity<Void> delete(@PathVariable Long pathwayId) {
        careerPathwayService.delete(pathwayId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{pathwayId}/resources")
    public CareerPathwayResponse linkResource(@PathVariable Long pathwayId,
                                               @Valid @RequestBody PathwayResourceLinkRequest request) {
        return careerPathwayService.linkResource(pathwayId, request);
    }

    @DeleteMapping("/{pathwayId}/resources/{resourceId}")
    public ResponseEntity<Void> unlinkResource(@PathVariable Long pathwayId, @PathVariable Long resourceId) {
        careerPathwayService.unlinkResource(pathwayId, resourceId);
        return ResponseEntity.noContent().build();
    }
}
