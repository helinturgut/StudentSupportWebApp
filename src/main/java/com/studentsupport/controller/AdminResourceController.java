package com.studentsupport.controller;

import com.studentsupport.dto.ResourceRequest;
import com.studentsupport.dto.ResourceResponse;
import com.studentsupport.security.AuthenticatedUser;
import com.studentsupport.service.ResourceService;
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
@RequestMapping("/api/admin/resources")
@RequiredArgsConstructor
public class AdminResourceController {

    private final ResourceService resourceService;

    @PostMapping
    public ResponseEntity<ResourceResponse> create(@AuthenticationPrincipal AuthenticatedUser principal,
                                                    @Valid @RequestBody ResourceRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(resourceService.create(principal.userId(), request));
    }

    @PutMapping("/{resourceId}")
    public ResourceResponse update(@PathVariable Long resourceId, @Valid @RequestBody ResourceRequest request) {
        return resourceService.update(resourceId, request);
    }

    @DeleteMapping("/{resourceId}")
    public ResponseEntity<Void> delete(@PathVariable Long resourceId) {
        resourceService.delete(resourceId);
        return ResponseEntity.noContent().build();
    }
}
