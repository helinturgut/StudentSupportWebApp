package com.studentsupport.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = "user")
@Entity
@Table(name = "cv_feedback")
public class CvFeedback {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long cvFeedbackId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String inputText;

    @Column(columnDefinition = "TEXT")
    private String feedbackText;

    @Column(columnDefinition = "boolean not null default false")
    @Builder.Default
    private boolean detailed = false;

    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
