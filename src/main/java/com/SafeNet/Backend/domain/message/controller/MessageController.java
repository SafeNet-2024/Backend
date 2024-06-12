package com.SafeNet.Backend.domain.message.controller;

import com.SafeNet.Backend.domain.message.dto.MessageDto;
import com.SafeNet.Backend.domain.message.service.MessageService;
import com.SafeNet.Backend.domain.messageroom.service.MessageRoomService;
import com.SafeNet.Backend.global.auth.JwtTokenProvider;
import com.SafeNet.Backend.global.pubsub.RedisPublisher;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;  // 로깅을 위해 추가
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;

import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Controller
@Tag(name = "Message", description = "Message API")
public class MessageController {
    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    private final RedisPublisher redisPublisher;
    private final MessageRoomService messageRoomService;
    private final MessageService messageService;


    // websocket "/pub/chat/message"로 들어오는 메시징을 처리한다.
    @MessageMapping("/chat/message")
    public void message(@RequestHeader(name = "ACCESS_TOKEN", required = false) String accessToken,
                        MessageDto messageDto) {
        try {
            log.debug("Received ACCESS_TOKEN: {}", accessToken);
            // Access Token 검증
            if (accessToken != null && accessToken.startsWith("Bearer ")) {
                String token = accessToken.substring(7);
                if (jwtTokenProvider.validateToken(token)) {
                    // 메시지 전송 로직 호출
                    messageRoomService.handleMessage(messageDto.getRoomId(), messageDto.getSender(), messageDto);
                } else {
                    throw new AccessDeniedException("Invalid or expired token");
                }
            } else {
                throw new AccessDeniedException("Missing or invalid ACCESS_TOKEN header!");
            }
        } catch (Exception e) {
            log.error("Failed to send message: {}", e.getMessage());
            throw e;
        }
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
