package com.SafeNet.Backend.domain.messageroom.dto;

import com.SafeNet.Backend.domain.member.entity.Member;
import com.SafeNet.Backend.domain.message.dto.MessageRequestDto;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.util.UUID;

@Getter
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class MessageRoomDto implements Serializable {
    @Serial
    private static final long serialVersionUID = 6494678977089006639L; // 역직렬화 위한 serialVersionUID 세팅

    private Long id;
    private String roomName;
    private String roomId;
    private String sender;
    private String receiver;
    private Long postId;

    public static MessageRoomDto createMessageRoom(MessageRequestDto messageRequestDto, Member member) {
        return MessageRoomDto.builder()
                .roomName(messageRequestDto.getReceiver())  // roomName을 receiver의 이름으로 설정
                .roomId(UUID.randomUUID().toString())
                .sender(member.getName())
                .receiver(messageRequestDto.getReceiver())
                .build();
    }
}