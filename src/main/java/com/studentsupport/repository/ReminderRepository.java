package com.studentsupport.repository;

import com.studentsupport.entity.Reminder;
import com.studentsupport.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ReminderRepository extends JpaRepository<Reminder, Long> {

    List<Reminder> findByUserOrderByDueDateAsc(User user);

    Optional<Reminder> findByReminderIdAndUser(Long reminderId, User user);

    void deleteByUser(User user);
}
