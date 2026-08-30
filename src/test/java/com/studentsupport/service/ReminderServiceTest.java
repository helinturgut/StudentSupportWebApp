package com.studentsupport.service;

import com.studentsupport.dto.ReminderRequest;
import com.studentsupport.dto.ReminderResponse;
import com.studentsupport.entity.Reminder;
import com.studentsupport.entity.User;
import com.studentsupport.exception.ResourceNotFoundException;
import com.studentsupport.repository.ReminderRepository;
import com.studentsupport.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReminderServiceTest {

    @Mock
    private ReminderRepository reminderRepository;
    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private ReminderService reminderService;

    private User user;

    @BeforeEach
    void setUp() {
        user = User.builder().userId(1L).fullName("Jane Doe").email("jane@example.com").build();
        lenientStub();
    }

    private void lenientStub() {
        lenient().when(userRepository.findById(1L)).thenReturn(Optional.of(user));
    }

    @Test
    void create_savesReminderForUser() {
        ReminderRequest request = new ReminderRequest();
        request.setTitle("Apply to internship");
        request.setDescription("Deadline for STEP");
        request.setDueDate(LocalDate.now().plusDays(5));

        when(reminderRepository.save(any(Reminder.class))).thenAnswer(inv -> {
            Reminder r = inv.getArgument(0);
            r.setReminderId(10L);
            return r;
        });

        ReminderResponse response = reminderService.create(1L, request);

        assertThat(response.getReminderId()).isEqualTo(10L);
        assertThat(response.getTitle()).isEqualTo("Apply to internship");
        assertThat(response.isCompleted()).isFalse();
    }

    @Test
    void create_unknownUser_throwsResourceNotFound() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());
        ReminderRequest request = new ReminderRequest();
        request.setTitle("x");
        request.setDueDate(LocalDate.now());

        assertThatThrownBy(() -> reminderService.create(99L, request))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void listForUser_returnsOnlyThatUsersReminders() {
        Reminder r1 = Reminder.builder().reminderId(1L).user(user).title("A").dueDate(LocalDate.now()).build();
        Reminder r2 = Reminder.builder().reminderId(2L).user(user).title("B").dueDate(LocalDate.now()).build();
        when(reminderRepository.findByUserOrderByDueDateAsc(user)).thenReturn(List.of(r1, r2));

        List<ReminderResponse> result = reminderService.listForUser(1L);

        assertThat(result).hasSize(2).extracting(ReminderResponse::getTitle).containsExactly("A", "B");
    }

    @Test
    void toggleComplete_flipsCompletedFlag() {
        Reminder reminder = Reminder.builder().reminderId(5L).user(user).title("A").dueDate(LocalDate.now())
                .completed(false).build();
        when(reminderRepository.findByReminderIdAndUser(5L, user)).thenReturn(Optional.of(reminder));
        when(reminderRepository.save(any(Reminder.class))).thenAnswer(inv -> inv.getArgument(0));

        ReminderResponse first = reminderService.toggleComplete(1L, 5L);
        assertThat(first.isCompleted()).isTrue();

        when(reminderRepository.findByReminderIdAndUser(5L, user)).thenReturn(Optional.of(reminder));
        ReminderResponse second = reminderService.toggleComplete(1L, 5L);
        assertThat(second.isCompleted()).isFalse();
    }

    @Test
    void toggleComplete_reminderBelongingToOtherUser_throwsResourceNotFound() {
        when(reminderRepository.findByReminderIdAndUser(5L, user)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> reminderService.toggleComplete(1L, 5L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void delete_removesOwnedReminder() {
        Reminder reminder = Reminder.builder().reminderId(5L).user(user).title("A").dueDate(LocalDate.now()).build();
        when(reminderRepository.findByReminderIdAndUser(5L, user)).thenReturn(Optional.of(reminder));

        reminderService.delete(1L, 5L);

        verify(reminderRepository).delete(reminder);
    }

    @Test
    void update_changesFieldsOnExistingReminder() {
        Reminder reminder = Reminder.builder().reminderId(5L).user(user).title("Old")
                .dueDate(LocalDate.now()).build();
        when(reminderRepository.findByReminderIdAndUser(5L, user)).thenReturn(Optional.of(reminder));
        when(reminderRepository.save(any(Reminder.class))).thenAnswer(inv -> inv.getArgument(0));

        ReminderRequest request = new ReminderRequest();
        request.setTitle("New title");
        request.setDescription("New description");
        request.setDueDate(LocalDate.now().plusDays(1));

        ReminderResponse response = reminderService.update(1L, 5L, request);

        assertThat(response.getTitle()).isEqualTo("New title");
        assertThat(response.getDescription()).isEqualTo("New description");
    }
}
