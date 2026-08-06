package com.studentsupport.service;

import com.studentsupport.dto.ChatMessageResponse;
import com.studentsupport.dto.ChatSessionResponse;
import com.studentsupport.entity.ChatMessage;
import com.studentsupport.entity.ChatSession;
import com.studentsupport.entity.MessageSender;
import com.studentsupport.entity.User;
import com.studentsupport.exception.ResourceNotFoundException;
import com.studentsupport.repository.ChatMessageRepository;
import com.studentsupport.repository.ChatSessionRepository;
import com.studentsupport.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class ChatService {

    private static final String DEFAULT_TITLE = "New chat";

    private final ChatSessionRepository chatSessionRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final UserRepository userRepository;
    private final FileStorageService fileStorageService;
    private final GeminiService geminiService;

    public List<ChatSessionResponse> listSessions(Long userId) {
        User user = getUser(userId);
        return chatSessionRepository.findByUserOrderByUpdatedAtDesc(user).stream()
                .map(this::toSessionResponse)
                .toList();
    }

    public ChatSessionResponse createSession(Long userId) {
        User user = getUser(userId);
        ChatSession session = chatSessionRepository.save(
                ChatSession.builder()
                        .user(user)
                        .title(DEFAULT_TITLE)
                        .build());
        return toSessionResponse(session);
    }

    public List<ChatMessageResponse> getMessages(Long userId, Long sessionId) {
        User user = getUser(userId);
        ChatSession session = getOwnedSession(user, sessionId);
        return chatMessageRepository.findBySessionOrderByCreatedAtAsc(session).stream()
                .map(this::toResponse)
                .toList();
    }

    public List<ChatMessageResponse> sendMessage(Long userId, Long sessionId, String messageText) {
        User user = getUser(userId);
        ChatSession session = getOwnedSession(user, sessionId);

        maybeUpdateTitle(session, messageText);

        ChatMessage userMessage = ChatMessage.builder()
                .session(session)
                .sender(MessageSender.USER)
                .messageText(messageText)
                .build();
        userMessage = chatMessageRepository.save(userMessage);

        List<ChatMessage> recentHistory = chatMessageRepository.findTop20BySessionOrderByCreatedAtDesc(session);
        Collections.reverse(recentHistory);
        GeminiService.AiResult result = geminiService.generateReply(recentHistory);

        ChatMessage botMessage = ChatMessage.builder()
                .session(session)
                .sender(MessageSender.BOT)
                .messageText(result.text())
                .build();
        botMessage = chatMessageRepository.save(botMessage);

        return List.of(toResponse(userMessage), toResponse(botMessage));
    }

    public void deleteSession(Long userId, Long sessionId) {
        User user = getUser(userId);
        ChatSession session = getOwnedSession(user, sessionId);

        List<ChatMessage> messages = chatMessageRepository.findBySessionOrderByCreatedAtAsc(session);
        for (ChatMessage message : messages) {
            fileStorageService.delete(message.getAttachmentStoredName());
        }

        chatMessageRepository.deleteBySession(session);
        chatSessionRepository.delete(session);
    }

    public ChatMessageResponse uploadAttachment(Long userId, Long sessionId, MultipartFile file) {
        User user = getUser(userId);
        ChatSession session = getOwnedSession(user, sessionId);

        String storedName = fileStorageService.store(file);
        String originalName = file.getOriginalFilename() != null ? file.getOriginalFilename() : storedName;

        maybeUpdateTitle(session, "Attached file: " + originalName);

        ChatMessage message = ChatMessage.builder()
                .session(session)
                .sender(MessageSender.USER)
                .messageText("Attached file: " + originalName)
                .attachmentFileName(originalName)
                .attachmentStoredName(storedName)
                .build();

        return toResponse(chatMessageRepository.save(message));
    }

    public Resource loadAttachment(Long userId, Long messageId) {
        User user = getUser(userId);
        ChatMessage message = chatMessageRepository.findById(messageId)
                .orElseThrow(() -> new ResourceNotFoundException("Message not found"));

        if (!message.getSession().getUser().getUserId().equals(user.getUserId())) {
            throw new ResourceNotFoundException("Message not found");
        }
        if (message.getAttachmentStoredName() == null) {
            throw new ResourceNotFoundException("Message has no attachment");
        }
        return fileStorageService.loadAsResource(message.getAttachmentStoredName());
    }

    private void maybeUpdateTitle(ChatSession session, String messageText) {
        if (session.getTitle() == null || DEFAULT_TITLE.equals(session.getTitle())) {
            String trimmed = messageText.trim();
            session.setTitle(trimmed.length() > 40 ? trimmed.substring(0, 40) + "…" : trimmed);
            chatSessionRepository.save(session);
        }
    }

    private ChatSession getOwnedSession(User user, Long sessionId) {
        return chatSessionRepository.findBySessionIdAndUser(sessionId, user)
                .orElseThrow(() -> new ResourceNotFoundException("Chat session not found"));
    }

    private User getUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }

    private ChatSessionResponse toSessionResponse(ChatSession session) {
        return ChatSessionResponse.builder()
                .sessionId(session.getSessionId())
                .title(session.getTitle())
                .startedAt(session.getStartedAt())
                .updatedAt(session.getUpdatedAt())
                .build();
    }

    private ChatMessageResponse toResponse(ChatMessage message) {
        return ChatMessageResponse.builder()
                .messageId(message.getMessageId())
                .sender(message.getSender())
                .messageText(message.getMessageText())
                .attachmentFileName(message.getAttachmentFileName())
                .attachmentUrl(message.getAttachmentStoredName() != null
                        ? "/api/chat/attachments/" + message.getMessageId()
                        : null)
                .createdAt(message.getCreatedAt())
                .build();
    }
}
