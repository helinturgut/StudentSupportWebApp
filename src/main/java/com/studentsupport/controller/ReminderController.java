package com.studentsupport.controller;

import com.studentsupport.dto.ReminderRequest;
import com.studentsupport.dto.ReminderResponse;
import com.studentsupport.security.AuthenticatedUser;
import com.studentsupport.service.ReminderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/reminders")
@RequiredArgsConstructor
public class ReminderController {

    private final ReminderService reminderService;

    @GetMapping
    public List<ReminderResponse> list(@AuthenticationPrincipal AuthenticatedUser principal) {
        return reminderService.listForUser(principal.userId());
    }

    @PostMapping
    public ResponseEntity<ReminderResponse> create(@AuthenticationPrincipal AuthenticatedUser principal,
                                                     @Valid @RequestBody ReminderRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(reminderService.create(principal.userId(), request));
    }

    @PutMapping("/{reminderId}")
    public ReminderResponse update(@AuthenticationPrincipal AuthenticatedUser principal,
                                    @PathVariable Long reminderId,
                                    @Valid @RequestBody ReminderRequest request) {
        return reminderService.update(principal.userId(), reminderId, request);
    }

    @PatchMapping("/{reminderId}/complete")
    public ReminderResponse toggleComplete(@AuthenticationPrincipal AuthenticatedUser principal,
                                            @PathVariable Long reminderId) {
        return reminderService.toggleComplete(principal.userId(), reminderId);
    }

    @DeleteMapping("/{reminderId}")
    public ResponseEntity<Void> delete(@AuthenticationPrincipal AuthenticatedUser principal,
                                        @PathVariable Long reminderId) {
        reminderService.delete(principal.userId(), reminderId);
        return ResponseEntity.noContent().build();
    }
}
