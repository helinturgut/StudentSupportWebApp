package com.studentsupport.service;

import com.studentsupport.dto.AuthResponse;
import com.studentsupport.dto.LoginRequest;
import com.studentsupport.dto.RegisterRequest;
import com.studentsupport.entity.PasswordResetToken;
import com.studentsupport.entity.RefreshToken;
import com.studentsupport.entity.Role;
import com.studentsupport.entity.RoleName;
import com.studentsupport.entity.StudentProfile;
import com.studentsupport.entity.User;
import com.studentsupport.entity.UserStatus;
import com.studentsupport.exception.BadRequestException;
import com.studentsupport.exception.UnauthorizedException;
import com.studentsupport.repository.PasswordResetTokenRepository;
import com.studentsupport.repository.RefreshTokenRepository;
import com.studentsupport.repository.RoleRepository;
import com.studentsupport.repository.StudentProfileRepository;
import com.studentsupport.repository.UserRepository;
import com.studentsupport.security.JwtService;
import com.studentsupport.security.TokenHasher;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final StudentProfileRepository studentProfileRepository;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final EmailService emailService;
    private final TokenHasher tokenHasher;

    @Value("${app.frontend-url}")
    private String frontendUrl;

    @Value("${jwt.refresh-expiration-ms:2592000000}")
    private long refreshExpirationMs;

    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new BadRequestException("An account with this email already exists");
        }

        Role studentRole = roleRepository.findByRoleName(RoleName.STUDENT)
                .orElseThrow(() -> new IllegalStateException("STUDENT role not found"));

        User user = User.builder()
                .fullName(request.getFullName())
                .email(request.getEmail())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .role(studentRole)
                .build();
        userRepository.save(user);

        studentProfileRepository.save(StudentProfile.builder()
                .user(user)
                .build());

        return buildAuthResponse(user, "Registration successful");
    }

    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new UnauthorizedException("Invalid email or password"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new UnauthorizedException("Invalid email or password");
        }

        if (user.getStatus() == UserStatus.PENDING_APPROVAL) {
            throw new UnauthorizedException("Please set your password using the link we emailed you");
        }

        if (user.getStatus() == UserStatus.REJECTED) {
            throw new UnauthorizedException("This account is not active");
        }

        if (user.getStatus() != UserStatus.ACTIVE) {
            throw new UnauthorizedException("This account is not active");
        }

        return buildAuthResponse(user, "Login successful");
    }

    @Transactional
    public AuthResponse refresh(String rawRefreshToken) {
        String hash = tokenHasher.hash(rawRefreshToken);
        RefreshToken refreshToken = refreshTokenRepository.findByTokenHash(hash)
                .orElseThrow(() -> new UnauthorizedException("Invalid or expired refresh token"));

        if (refreshToken.isRevoked() || refreshToken.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new UnauthorizedException("Invalid or expired refresh token");
        }

        refreshToken.setRevoked(true);
        refreshTokenRepository.save(refreshToken);

        return buildAuthResponse(refreshToken.getUser(), "Token refreshed");
    }

    @Transactional
    public void logout(String rawRefreshToken) {
        String hash = tokenHasher.hash(rawRefreshToken);
        refreshTokenRepository.findByTokenHash(hash).ifPresent(refreshToken -> {
            refreshToken.setRevoked(true);
            refreshTokenRepository.save(refreshToken);
        });
    }

    @Transactional
    public void forgotPassword(String email) {
        userRepository.findByEmail(email).ifPresent(user -> {
            passwordResetTokenRepository.deleteByUser(user);

            String rawToken = UUID.randomUUID().toString();
            passwordResetTokenRepository.save(PasswordResetToken.builder()
                    .user(user)
                    .tokenHash(tokenHasher.hash(rawToken))
                    .expiresAt(LocalDateTime.now().plusMinutes(30))
                    .build());

            String resetLink = frontendUrl + "/reset-password?token=" + rawToken;
            emailService.sendPasswordResetEmail(user.getEmail(), resetLink);
        });

    }

    @Transactional
    public void resetPassword(String rawToken, String newPassword) {
        PasswordResetToken resetToken = passwordResetTokenRepository.findByTokenHash(tokenHasher.hash(rawToken))
                .orElseThrow(() -> new BadRequestException("This reset link is invalid or has expired"));

        if (resetToken.isUsed() || resetToken.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new BadRequestException("This reset link is invalid or has expired");
        }

        User user = resetToken.getUser();
        user.setPasswordHash(passwordEncoder.encode(newPassword));
        if (user.getStatus() == UserStatus.PENDING_APPROVAL) {
            user.setStatus(UserStatus.ACTIVE);
        }
        userRepository.save(user);

        resetToken.setUsed(true);
        passwordResetTokenRepository.save(resetToken);
    }

    private AuthResponse buildAuthResponse(User user, String message) {
        RoleName role = user.getRole().getRoleName();
        String token = jwtService.generateToken(user.getUserId(), user.getEmail(), role);
        String rawRefreshToken = issueRefreshToken(user);

        return AuthResponse.builder()
                .message(message)
                .userId(user.getUserId())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .role(role.name())
                .token(token)
                .refreshToken(rawRefreshToken)
                .build();
    }

    private String issueRefreshToken(User user) {
        String rawRefreshToken = UUID.randomUUID().toString();
        refreshTokenRepository.save(RefreshToken.builder()
                .user(user)
                .tokenHash(tokenHasher.hash(rawRefreshToken))
                .expiresAt(LocalDateTime.now().plus(Duration.ofMillis(refreshExpirationMs)))
                .build());
        return rawRefreshToken;
    }
}
