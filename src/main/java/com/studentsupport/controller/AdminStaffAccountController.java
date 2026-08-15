package com.studentsupport.controller;

import com.studentsupport.dto.CreateStaffAccountRequest;
import com.studentsupport.dto.StaffAccountResponse;
import com.studentsupport.service.AdminStaffAccountService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/admin/staff-accounts")
@RequiredArgsConstructor
public class AdminStaffAccountController {

    private final AdminStaffAccountService adminStaffAccountService;

    @GetMapping
    public List<StaffAccountResponse> listInvited() {
        return adminStaffAccountService.listInvited();
    }

    @GetMapping("/active")
    public List<StaffAccountResponse> listActive() {
        return adminStaffAccountService.listActive();
    }

    @PostMapping
    public ResponseEntity<StaffAccountResponse> createStaffAccount(@Valid @RequestBody CreateStaffAccountRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(adminStaffAccountService.createStaffAccount(request.getFullName(), request.getEmail()));
    }

    @PostMapping("/{userId}/resend-invite")
    public StaffAccountResponse resendInvite(@PathVariable Long userId) {
        return adminStaffAccountService.resendInvite(userId);
    }

    @DeleteMapping("/{userId}")
    public ResponseEntity<Void> deletePermanently(@PathVariable Long userId) {
        adminStaffAccountService.deletePermanently(userId);
        return ResponseEntity.noContent().build();
    }
}
