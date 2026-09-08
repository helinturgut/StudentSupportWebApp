package com.studentsupport.service;

import com.studentsupport.dto.AccountResponse;
import com.studentsupport.entity.ChatSession;
import com.studentsupport.entity.RoleName;
import com.studentsupport.entity.User;
import com.studentsupport.exception.BadRequestException;
import com.studentsupport.exception.ResourceNotFoundException;
import com.studentsupport.exception.UnauthorizedException;
import com.studentsupport.repository.ChatMessageRepository;
import com.studentsupport.repository.ChatSessionRepository;
import com.studentsupport.repository.CvFeedbackRepository;
import com.studentsupport.repository.PasswordResetTokenRepository;
import com.studentsupport.repository.RecommendationRepository;
import com.studentsupport.repository.RefreshTokenRepository;
import com.studentsupport.repository.ReminderRepository;
import com.studentsupport.repository.ResourceRepository;
import com.studentsupport.repository.StudentProfileRepository;
import com.studentsupport.repository.UserFeedbackRepository;
import com.studentsupport.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class AccountService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final StudentProfileRepository studentProfileRepository;
    private final ReminderRepository reminderRepository;
    private final UserFeedbackRepository userFeedbackRepository;
    private final CvFeedbackRepository cvFeedbackRepository;
    private final RecommendationRepository recommendationRepository;
    private final ChatSessionRepository chatSessionRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final ResourceRepository resourceRepository;

    public AccountResponse getAccount(Long userId) {
        return toResponse(getUser(userId));
    }

    public AccountResponse updateFullName(Long userId, String fullName) {
        User user = getUser(userId);
        user.setFullName(fullName);
        return toResponse(userRepository.save(user));
    }

    public void changePassword(Long userId, String currentPassword, String newPassword) {
        User user = getUser(userId);
        if (!passwordEncoder.matches(currentPassword, user.getPasswordHash())) {
            throw new UnauthorizedException("Current password is incorrect");
        }
        user.setPasswordHash(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }

    public void deleteAccount(Long userId, String password) {
        User user = getUser(userId);
        if (!passwordEncoder.matches(password, user.getPasswordHash())) {
            throw new UnauthorizedException("Password is incorrect");
        }

        if (user.getRole().getRoleName() == RoleName.ADMIN) {
            throw new BadRequestException("The admin account cannot be deleted.");
        }

        if (resourceRepository.existsByCreatedBy(user)) {
            throw new BadRequestException(
                    "This account can't be deleted because it has published resources. Reassign or remove them first.");
        }

        refreshTokenRepository.deleteByUser(user);
        passwordResetTokenRepository.deleteByUser(user);
        studentProfileRepository.deleteByUser(user);
        reminderRepository.deleteByUser(user);
        userFeedbackRepository.deleteByUser(user);
        cvFeedbackRepository.deleteByUser(user);
        recommendationRepository.deleteByUser(user);

        var chatSessions = chatSessionRepository.findByUserOrderByUpdatedAtDesc(user);
        for (ChatSession session : chatSessions) {
            chatMessageRepository.deleteBySession(session);
        }
        chatSessionRepository.deleteAll(chatSessions);

        userRepository.delete(user);
    }

    private User getUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }

    private AccountResponse toResponse(User user) {
        return AccountResponse.builder()
                .userId(user.getUserId())
                .fullName(user.getFullName())
                .email(user.getEmail())
                .build();
    }
}
