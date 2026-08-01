package com.studentsupport.repository;

import com.studentsupport.entity.ChatMessage;
import com.studentsupport.entity.ChatSession;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {

    List<ChatMessage> findBySessionOrderByCreatedAtAsc(ChatSession session);

    // Last 20 messages in reverse order for AI context window (most recent first)
    List<ChatMessage> findTop20BySessionOrderByCreatedAtDesc(ChatSession session);

    List<ChatMessage> findBySafetyFlagTrue();
}
