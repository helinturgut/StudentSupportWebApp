package com.studentsupport.service;

import com.studentsupport.dto.StaffAccountResponse;
import com.studentsupport.entity.PasswordResetToken;
import com.studentsupport.entity.Role;
import com.studentsupport.entity.RoleName;
import com.studentsupport.entity.User;
import com.studentsupport.entity.UserStatus;
import com.studentsupport.exception.BadRequestException;
import com.studentsupport.exception.ResourceNotFoundException;
import com.studentsupport.repository.PasswordResetTokenRepository;
import com.studentsupport.repository.RefreshTokenRepository;
import com.studentsupport.repository.RoleRepository;
import com.studentsupport.repository.UserRepository;
import com.studentsupport.security.TokenHasher;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AdminStaffAccountService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;
    private final TokenHasher tokenHasher;

    @Value("${app.frontend-url}")
    private String frontendUrl;

    public List<StaffAccountResponse> listInvited() {
        return userRepository.findByRole_RoleNameAndStatus(RoleName.CAREER_SUPPORT_STAFF, UserStatus.PENDING_APPROVAL)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public List<StaffAccountResponse> listActive() {
        return userRepository.findByRole_RoleNameAndStatus(RoleName.CAREER_SUPPORT_STAFF, UserStatus.ACTIVE)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public StaffAccountResponse createStaffAccount(String fullName, String email) {
        if (userRepository.existsByEmail(email)) {
            throw new BadRequestException("An account with this email already exists");
        }

        Role staffRole = roleRepository.findByRoleName(RoleName.CAREER_SUPPORT_STAFF)
                .orElseThrow(() -> new IllegalStateException("CAREER_SUPPORT_STAFF role not found"));

        User user = User.builder()
                .fullName(fullName)
                .email(email)
                .passwordHash(passwordEncoder.encode(UUID.randomUUID().toString()))
                .role(staffRole)
                .status(UserStatus.PENDING_APPROVAL)
                .build();
        userRepository.save(user);

        sendInvite(user);

        return toResponse(user);
    }

    @Transactional
    public StaffAccountResponse resendInvite(Long userId) {
        User user = findInvitedStaffMember(userId);
        passwordResetTokenRepository.deleteByUser(user);
        sendInvite(user);
        return toResponse(user);
    }

    @Transactional
    public void deletePermanently(Long userId) {
        User user = findStaffMember(userId);
        refreshTokenRepository.deleteByUser(user);
        passwordResetTokenRepository.deleteByUser(user);
        userRepository.delete(user);
    }

    private void sendInvite(User user) {
        String rawToken = UUID.randomUUID().toString();
        passwordResetTokenRepository.save(PasswordResetToken.builder()
                .user(user)
                .tokenHash(tokenHasher.hash(rawToken))
                .expiresAt(LocalDateTime.now().plusDays(7))
                .build());

        String createPasswordLink = frontendUrl + "/reset-password?token=" + rawToken + "&welcome=true";
        emailService.sendStaffWelcomeEmail(user.getEmail(), user.getFullName(), createPasswordLink);
    }

    private User findInvitedStaffMember(Long userId) {
        User user = findStaffMember(userId);

        if (user.getStatus() != UserStatus.PENDING_APPROVAL) {
            throw new BadRequestException("This staff account has already been activated");
        }

        return user;
    }

    private User findStaffMember(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Staff account not found"));

        if (user.getRole().getRoleName() != RoleName.CAREER_SUPPORT_STAFF) {
            throw new ResourceNotFoundException("Staff account not found");
        }

        return user;
    }

    private StaffAccountResponse toResponse(User user) {
        return StaffAccountResponse.builder()
                .userId(user.getUserId())
                .fullName(user.getFullName())
                .email(user.getEmail())
                .status(user.getStatus().name())
                .createdAt(user.getCreatedAt())
                .build();
    }
}
