package com.studentsupport.controller;

import com.studentsupport.dto.AuthResponse;
import com.studentsupport.dto.ForgotPasswordRequest;
import com.studentsupport.dto.LoginRequest;
import com.studentsupport.dto.RefreshTokenRequest;
import com.studentsupport.dto.RegisterRequest;
import com.studentsupport.dto.ResetPasswordRequest;
import com.studentsupport.service.AuthService;
import com.studentsupport.service.RateLimiterService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Duration;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private static final int LOGIN_MAX_ATTEMPTS = 10;
    private static final Duration LOGIN_WINDOW = Duration.ofMinutes(15);
    private static final int FORGOT_PASSWORD_MAX_ATTEMPTS = 5;
    private static final Duration FORGOT_PASSWORD_WINDOW = Duration.ofHours(1);

    private final AuthService authService;
    private final RateLimiterService rateLimiterService;

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(authService.register(request));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request, HttpServletRequest httpRequest) {
        rateLimiterService.checkAllowed("login:" + clientIp(httpRequest), LOGIN_MAX_ATTEMPTS, LOGIN_WINDOW);
        return ResponseEntity.ok(authService.login(request));
    }

    @PostMapping("/refresh")
    public ResponseEntity<AuthResponse> refresh(@Valid @RequestBody RefreshTokenRequest request) {
        return ResponseEntity.ok(authService.refresh(request.getRefreshToken()));
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@Valid @RequestBody RefreshTokenRequest request) {
        authService.logout(request.getRefreshToken());
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<Void> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request,
                                                HttpServletRequest httpRequest) {
        rateLimiterService.checkAllowed(
                "forgot-password:" + clientIp(httpRequest), FORGOT_PASSWORD_MAX_ATTEMPTS, FORGOT_PASSWORD_WINDOW);
        authService.forgotPassword(request.getEmail());
        return ResponseEntity.ok().build();
    }

    @PostMapping("/reset-password")
    public ResponseEntity<Void> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        authService.resetPassword(request.getToken(), request.getNewPassword());
        return ResponseEntity.ok().build();
    }

    private String clientIp(HttpServletRequest request) {
        return request.getRemoteAddr();
    }
}
