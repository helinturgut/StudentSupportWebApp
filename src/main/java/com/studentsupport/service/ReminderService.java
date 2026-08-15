package com.studentsupport.service;

import com.studentsupport.dto.ReminderRequest;
import com.studentsupport.dto.ReminderResponse;
import com.studentsupport.entity.Reminder;
import com.studentsupport.entity.User;
import com.studentsupport.exception.ResourceNotFoundException;
import com.studentsupport.repository.ReminderRepository;
import com.studentsupport.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class ReminderService {

    private final ReminderRepository reminderRepository;
    private final UserRepository userRepository;

    public List<ReminderResponse> listForUser(Long userId) {
        User user = getUser(userId);
        return reminderRepository.findByUserOrderByDueDateAsc(user).stream()
                .map(this::toResponse)
                .toList();
    }

    public ReminderResponse create(Long userId, ReminderRequest request) {
        User user = getUser(userId);
        Reminder reminder = Reminder.builder()
                .user(user)
                .title(request.getTitle())
                .description(request.getDescription())
                .dueDate(request.getDueDate())
                .build();
        return toResponse(reminderRepository.save(reminder));
    }

    public ReminderResponse update(Long userId, Long reminderId, ReminderRequest request) {
        Reminder reminder = getOwnedReminder(userId, reminderId);
        reminder.setTitle(request.getTitle());
        reminder.setDescription(request.getDescription());
        reminder.setDueDate(request.getDueDate());
        return toResponse(reminderRepository.save(reminder));
    }

    public ReminderResponse toggleComplete(Long userId, Long reminderId) {
        Reminder reminder = getOwnedReminder(userId, reminderId);
        reminder.setCompleted(!reminder.isCompleted());
        return toResponse(reminderRepository.save(reminder));
    }

    public void delete(Long userId, Long reminderId) {
        Reminder reminder = getOwnedReminder(userId, reminderId);
        reminderRepository.delete(reminder);
    }

    private Reminder getOwnedReminder(Long userId, Long reminderId) {
        User user = getUser(userId);
        return reminderRepository.findByReminderIdAndUser(reminderId, user)
                .orElseThrow(() -> new ResourceNotFoundException("Reminder not found"));
    }

    private User getUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }

    private ReminderResponse toResponse(Reminder reminder) {
        return ReminderResponse.builder()
                .reminderId(reminder.getReminderId())
                .title(reminder.getTitle())
                .description(reminder.getDescription())
                .dueDate(reminder.getDueDate())
                .completed(reminder.isCompleted())
                .createdAt(reminder.getCreatedAt())
                .build();
    }
}
