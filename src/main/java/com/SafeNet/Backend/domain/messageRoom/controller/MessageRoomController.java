package com.SafeNet.Backend.domain.messageroom.controller;

import com.SafeNet.Backend.domain.member.entity.UserDetailsImpl;
import com.SafeNet.Backend.domain.message.dto.MessageResponseDto;
import com.SafeNet.Backend.domain.messageroom.dto.MessageRoomDto;
import com.SafeNet.Backend.domain.messageroom.service.MessageRoomService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@Tag(name = "MessageRoom", description = "게시물을 통해 채팅방을 생성할 때 사용하는 API")
public class MessageRoomController {
    private final MessageRoomService messageRoomService;

    @PostMapping("/api/v2/posts/{postId}/rooms")
    @Operation(summary = "채팅방 생성", description = "게시물에서 판매자와 채팅시 사용하는 API")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "채팅방이 성공적으로 생성되었습니다.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = MessageResponseDto.class))),
    })
    public MessageResponseDto createRoom(
            @RequestHeader(name = "ACCESS_TOKEN", required = false) String accessToken,
            @RequestHeader(name = "REFRESH_TOKEN", required = false) String refreshToken,
            @PathVariable(name = "postId") Long postId) {
        String email = getUserEmail();
        return messageRoomService.createRoom(postId, email);
    }

    @GetMapping("/api/v2/rooms")
    @Operation(summary = "채팅방 리스트 조회", description = "로그인한 사용자와 관련된 모든 채팅방을 조회시 사용하는 API. 채팅방의 이름은 상대방의 이름으로 설정됩니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "채팅방 리스트 조회 성공", content = @Content(mediaType = "application/json", schema = @Schema(implementation = MessageResponseDto[].class)))
    })
    public List<MessageResponseDto> findAllRoomByUser(
            @RequestHeader(name = "ACCESS_TOKEN", required = false) String accessToken,
            @RequestHeader(name = "REFRESH_TOKEN", required = false) String refreshToken) {
        String email = getUserEmail();
        return messageRoomService.findAllRoomByUser(email);
    }

    @GetMapping("/api/v2/rooms/{roomId}")
    @Operation(summary = "채팅방 조회", description = "특정 채팅방의 상세 정보를 조회할 때 사용하는 API")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "채팅방 정보가 성공적으로 조회되었습니다.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = MessageRoomDto.class))),
    })
    public MessageRoomDto findRoom(
            @RequestHeader(name = "ACCESS_TOKEN", required = false) String accessToken,
            @RequestHeader(name = "REFRESH_TOKEN", required = false) String refreshToken,
            @PathVariable(name = "roomId") String roomId) {
        String email = getUserEmail();
        return messageRoomService.findRoom(roomId, email);
    }

    @DeleteMapping("/api/v2/rooms/{roomId}")
    @Operation(summary = "특정 채팅방 삭제", description = "채팅방 삭제시 사용되는 API. " +
            "member가 sender인 경우 삭제시 sender 이름이 Not_Exist_Sender로 변경되고," +
            "member가 receiver인 경우 삭제시 Not_Exist_Receiver로 변경됩니다." +
            "sender와 receiver 모두 삭제할 경우 채팅방은 db와 redis에서 완전히 삭제됩니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "채팅방 삭제 성공", content = @Content(mediaType = "application/json")),
    })
    public MessageResponseDto deleteRoom(
            @RequestHeader(name = "ACCESS_TOKEN", required = false) String accessToken,
            @RequestHeader(name = "REFRESH_TOKEN", required = false) String refreshToken,
            @PathVariable(name = "roomId") String roomId) {
        String email = getUserEmail();
        return messageRoomService.deleteRoom(roomId, email);
    }

    private static String getUserEmail() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        UserDetailsImpl userDetails = (UserDetailsImpl) principal;
        return userDetails.getUsername();
    }
}