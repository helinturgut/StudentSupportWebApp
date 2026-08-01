package com.studentsupport.repository;

import com.studentsupport.entity.ChatSession;
import com.studentsupport.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ChatSessionRepository extends JpaRepository<ChatSession, Long> {

    List<ChatSession> findByUserOrderByUpdatedAtDesc(User user);

    Optional<ChatSession> findBySessionIdAndUser(Long sessionId, User user);

    long countByUser(User user);
}
