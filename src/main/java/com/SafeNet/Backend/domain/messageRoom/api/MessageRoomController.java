package com.SafeNet.Backend.domain.messageroom.api;

import com.SafeNet.Backend.domain.member.domain.Member;
import com.SafeNet.Backend.domain.message.dto.MessageRequestDto;
import com.SafeNet.Backend.domain.message.dto.MessageResponseDto;
import com.SafeNet.Backend.domain.messageroom.dto.MessageRoomDto;
import com.SafeNet.Backend.domain.messageroom.service.MessageRoomService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/rooms")
@RequiredArgsConstructor
@Tag(name = "MessageRoom", description = "MessageRoom API")
public class MessageRoomController {
    private final MessageRoomService messageRoomService;

    @PostMapping
    @Operation(summary = "채팅방 생성", description = "게시물에서 판매자와 채팅시 사용하는 API")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "채팅방이 성공적으로 생성되었습니다.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = MessageResponseDto.class))),
    })
    @Parameters({
            @Parameter(name = "receiver", description = "수신자의 사용자 이름", example = "userB"),
            @Parameter(name = "postId", description = "게시물의 고유 ID", example = "101")
    })
    public MessageResponseDto createRoom(@RequestBody MessageRequestDto messageRequestDto) {
        // TODO accessToken에서 사용자 이름을 추출하는 로직 추가
        return messageRoomService.createRoom(messageRequestDto, Member.builder().name("userA").build());
    }

    @GetMapping
    @Operation(summary = "채팅방 리스트 조회", description = "로그인한 사용자와 관련된 모든 채팅방을 조회시 사용하는 API. 채팅방의 이름은 상대방의 이름으로 설정됩니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "채팅방 리스트 조회 성공", content = @Content(mediaType = "application/json", schema = @Schema(implementation = MessageResponseDto[].class)))
    })
    public List<MessageResponseDto> findAllRoomByUser() {
        return messageRoomService.findAllRoomByUser(Member.builder().name("userA").build());
    }

    @GetMapping("/{roomId}")
    @Operation(summary = "채팅방 조회", description = "특정 채팅방의 상세 정보를 조회할 때 사용하는 API")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "채팅방 정보가 성공적으로 조회되었습니다.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = MessageRoomDto.class))),
    })
    @Parameter(name = "roomId", description = "조회할 채팅방의 ID", required = true, example = "123")
    public MessageRoomDto findRoom(@PathVariable String roomId) {
        return messageRoomService.findRoom(roomId, Member.builder().name("userA").build());
    }

    @DeleteMapping("/{roomId}")
    @Operation(summary = "특정 채팅방 삭제", description = "채팅방 삭제시 사용되는 API. 채팅방의 sender 또는 receiver만이 채팅방을 삭제할 수 있고, sender와 receiver 모두 삭제 요청을 할 경우 채팅방은 완전히 삭제됩니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "채팅방 삭제 성공", content = @Content(mediaType = "application/json")),
    })
    @Parameter(name = "roomId", description = "삭제할 채팅방의 ID", required = true, example = "123")
    public MessageResponseDto deleteRoom(@PathVariable String roomId) {
        return messageRoomService.deleteRoom(roomId, Member.builder().name("userA").build());
    }
}