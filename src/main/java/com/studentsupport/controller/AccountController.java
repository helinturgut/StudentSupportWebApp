package com.studentsupport.controller;

import com.studentsupport.dto.AccountResponse;
import com.studentsupport.dto.ChangePasswordRequest;
import com.studentsupport.dto.DeleteAccountRequest;
import com.studentsupport.dto.UpdateAccountRequest;
import com.studentsupport.security.AuthenticatedUser;
import com.studentsupport.service.AccountService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/account")
@RequiredArgsConstructor
public class AccountController {

    private final AccountService accountService;

    @GetMapping
    public AccountResponse getAccount(@AuthenticationPrincipal AuthenticatedUser principal) {
        return accountService.getAccount(principal.userId());
    }

    @PutMapping("/name")
    public AccountResponse updateFullName(@AuthenticationPrincipal AuthenticatedUser principal,
                                           @Valid @RequestBody UpdateAccountRequest request) {
        return accountService.updateFullName(principal.userId(), request.getFullName());
    }

    @PutMapping("/password")
    public ResponseEntity<Void> changePassword(@AuthenticationPrincipal AuthenticatedUser principal,
                                                @Valid @RequestBody ChangePasswordRequest request) {
        accountService.changePassword(principal.userId(), request.getCurrentPassword(), request.getNewPassword());
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping
    public ResponseEntity<Void> deleteAccount(@AuthenticationPrincipal AuthenticatedUser principal,
                                               @Valid @RequestBody DeleteAccountRequest request) {
        accountService.deleteAccount(principal.userId(), request.getPassword());
        return ResponseEntity.noContent().build();
    }
}
