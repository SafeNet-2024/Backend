package com.SafeNet.Backend.domain.message.controller;

import com.SafeNet.Backend.domain.message.dto.MessageDto;
import com.SafeNet.Backend.domain.message.service.MessageService;
import com.SafeNet.Backend.domain.messageroom.service.MessageRoomService;
import com.SafeNet.Backend.global.pubsub.RedisPublisher;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;

import java.time.LocalDateTime;
import java.util.List;

@RequiredArgsConstructor
@Controller
@Tag(name = "Message", description = "Message API")
public class MessageController {
    private final RedisPublisher redisPublisher;
    private final MessageRoomService messageRoomService;
    private final MessageService messageService;

    // websocket "/pub/chat/message"로 들어오는 메시징을 처리한다.

    @MessageMapping("/chat/message")
    @Operation(summary = "메시지 발송", description = "WebSocket을 통해 메시지를 발송한다")
    public void message(@RequestHeader(name = "ACCESS_TOKEN", required = false) String accessToken,
                        @RequestHeader(name = "REFRESH_TOKEN", required = false) String refreshToken,
                        MessageDto messageDto) {
        // 클라이언트 채팅방(topic) 입장, 대화를 위해 리스너와 연동
        messageRoomService.enterMessageRoom(messageDto.getRoomId());

        // MessageDto 객체를 빌더를 통해 생성하고 현재 시간을 설정
        MessageDto messageWithTime = MessageDto.builder()
                .sender(messageDto.getSender())
                .roomId(messageDto.getRoomId())
                .message(messageDto.getMessage())
                .sentTime(LocalDateTime.now().toString())
                .build();

        // Websocket에 발행된 메시지를 redis로 발행한다(publish)
        // 해당 쪽지방을 구독(subscribe)한 클라이언트에게 메시지가 실시간 전송
        redisPublisher.publish(messageRoomService.getTopic(messageDto.getRoomId()), messageWithTime);

        // DB와 Redis에 메시지 저장
        messageService.saveMessage(messageDto);
    }

    // 대화 내역 조회
    @GetMapping("/api/rooms/{roomId}/message")
    @Operation(summary = "채팅방 메시지 로드", description = "지정된 채팅방의 모든 메시지를 로드합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "메시지가 성공적으로 로드되었습니다.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = MessageDto[].class))),
            @ApiResponse(responseCode = "404", description = "채팅방을 찾을 수 없습니다.", content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "500", description = "서버 오류", content = @Content(mediaType = "application/json"))
    })
    @Parameter(name = "roomId", description = "메시지를 조회할 채팅방의 ID", required = true, example = "123")
    public ResponseEntity<List<MessageDto>> loadMessage(
            @RequestHeader(name = "ACCESS_TOKEN", required = false) String accessToken,
            @RequestHeader(name = "REFRESH_TOKEN", required = false) String refreshToken,
            @PathVariable String roomId) {
        return ResponseEntity.ok(messageService.loadMessage(roomId));
    }
}
