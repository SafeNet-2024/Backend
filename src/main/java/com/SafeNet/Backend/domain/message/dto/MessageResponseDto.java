package com.SafeNet.Backend.domain.message.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class MessageResponseDto {
    private Long id;
    private String roomName;
    private String roomId;
    private String sender;
    private String receiver;
    private Long postId;
    private String message;
    private LocalDateTime createdAt;
    private int status;
}

