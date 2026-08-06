package com.studentsupport.controller;

import com.studentsupport.dto.ChatMessageRequest;
import com.studentsupport.dto.ChatMessageResponse;
import com.studentsupport.dto.ChatSessionResponse;
import com.studentsupport.security.AuthenticatedUser;
import com.studentsupport.service.ChatService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
public class ChatController {

    private final ChatService chatService;

    @GetMapping("/sessions")
    public List<ChatSessionResponse> listSessions(@AuthenticationPrincipal AuthenticatedUser principal) {
        return chatService.listSessions(principal.userId());
    }

    @PostMapping("/sessions")
    public ResponseEntity<ChatSessionResponse> createSession(@AuthenticationPrincipal AuthenticatedUser principal) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(chatService.createSession(principal.userId()));
    }

    @DeleteMapping("/sessions/{sessionId}")
    public ResponseEntity<Void> deleteSession(@AuthenticationPrincipal AuthenticatedUser principal,
                                               @PathVariable Long sessionId) {
        chatService.deleteSession(principal.userId(), sessionId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/sessions/{sessionId}/messages")
    public List<ChatMessageResponse> getMessages(@AuthenticationPrincipal AuthenticatedUser principal,
                                                  @PathVariable Long sessionId) {
        return chatService.getMessages(principal.userId(), sessionId);
    }

    @PostMapping("/sessions/{sessionId}/messages")
    public ResponseEntity<List<ChatMessageResponse>> sendMessage(@AuthenticationPrincipal AuthenticatedUser principal,
                                                                    @PathVariable Long sessionId,
                                                                    @Valid @RequestBody ChatMessageRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(chatService.sendMessage(principal.userId(), sessionId, request.getMessageText()));
    }

    @PostMapping(value = "/sessions/{sessionId}/upload", consumes = "multipart/form-data")
    public ResponseEntity<ChatMessageResponse> uploadFile(@AuthenticationPrincipal AuthenticatedUser principal,
                                                            @PathVariable Long sessionId,
                                                            @RequestParam("file") MultipartFile file) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(chatService.uploadAttachment(principal.userId(), sessionId, file));
    }

    @GetMapping("/attachments/{messageId}")
    public ResponseEntity<Resource> downloadAttachment(@AuthenticationPrincipal AuthenticatedUser principal,
                                                         @PathVariable Long messageId) {
        Resource resource = chatService.loadAttachment(principal.userId(), messageId);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + resource.getFilename() + "\"")
                .body(resource);
    }
}
