package com.studentsupport.service;

import com.studentsupport.dto.AccountResponse;
import com.studentsupport.entity.User;
import com.studentsupport.entity.UserStatus;
import com.studentsupport.exception.ResourceNotFoundException;
import com.studentsupport.exception.UnauthorizedException;
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
        user.setStatus(UserStatus.INACTIVE);
        userRepository.save(user);
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
