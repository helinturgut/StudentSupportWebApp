package com.studentsupport.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Builder
public class ReminderResponse {

    private Long reminderId;
    private String title;
    private String description;
    private LocalDate dueDate;
    private boolean completed;
    private LocalDateTime createdAt;
}
