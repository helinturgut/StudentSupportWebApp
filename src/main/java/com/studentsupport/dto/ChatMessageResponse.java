package com.studentsupport.dto;

import com.studentsupport.entity.MessageSender;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class ChatMessageResponse {

    private Long messageId;
    private MessageSender sender;
    private String messageText;
    private String attachmentFileName;
    private String attachmentUrl;
    private LocalDateTime createdAt;
}
