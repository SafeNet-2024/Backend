package com.SafeNet.Backend.domain.message.service;

import com.SafeNet.Backend.domain.member.domain.Member;
import com.SafeNet.Backend.domain.member.repository.MemberRepository;
import com.SafeNet.Backend.domain.message.domain.Message;
import com.SafeNet.Backend.domain.message.dto.MessageDto;
import com.SafeNet.Backend.domain.message.dto.MessageRequestDto;
import com.SafeNet.Backend.domain.message.dto.MessageResponseDto;
import com.SafeNet.Backend.domain.message.repository.MessageRepository;
import com.SafeNet.Backend.domain.messageRoom.domain.MessageRoom;
import com.SafeNet.Backend.domain.messageRoom.repository.MessageRoomRepository;
import com.SafeNet.Backend.domain.messageRoom.service.MessageRoomService;
import com.SafeNet.Backend.domain.post.domain.Post;
import com.SafeNet.Backend.domain.post.repository.PostRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;


@SpringBootTest
@Transactional
class MessageServiceTest {
    @Autowired
    private MessageService messageService;

    @Autowired
    private MessageRepository messageRepository;

    @Autowired
    private MessageRoomRepository messageRoomRepository;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private MessageRoomService messageRoomService;

    @Autowired
    private RedisTemplate<String, MessageDto> redisTemplateMessage;

    private Member userA;
    private MessageResponseDto room1;

    @BeforeEach
    void setUp() {
        messageRepository.deleteAll();

        // 사용자와 게시물 미리 설정
        userA = Member.builder().name("userA").build();
        memberRepository.save(userA);

        Post post = Post.builder().member(userA).build();
        postRepository.save(post);

        // 새 쪽지방 요청
        MessageRequestDto requestDto = MessageRequestDto.builder()
                .receiver("userB")
                .postId(post.getId())
                .build();
        // 실행
        room1 = messageRoomService.createRoom(requestDto, userA);
    }

    @AfterEach
    void tearDown() {
        // 테스트가 끝난 후 Redis 데이터 정리
        redisTemplateMessage.delete(room1.getRoomId());
        messageRepository.deleteAll();
    }

    @Test
    void testSaveMessage() {
        MessageDto messageDto = MessageDto.builder()
                .sender(userA.getName())
                .roomId(room1.getRoomId())
                .message("First Message")
                .build();

        MessageDto messageDto2 = MessageDto.builder()
                .sender(userA.getName())
                .roomId(room1.getRoomId())
                .message("Second Message")
                .build();

        messageService.saveMessage(messageDto);
        messageService.saveMessage(messageDto2);

        // Redis에서 메시지 검증
        List<MessageDto> messages = redisTemplateMessage.opsForList().range(room1.getRoomId(), 0, -1);

        // 메시지 리스트가 null이 아니고 비어 있지 않은지 확인
        assertNotNull(messages, "Message list should not be null");
        assertFalse(messages.isEmpty(), "No messages found in Room '" + room1.getRoomId() + "'");

        assertEquals("First Message", messages.get(0).getMessage());
        assertEquals("Second Message", messages.get(1).getMessage());

        messages.forEach(message ->
                System.out.println("Sender: " + message.getSender() + ", Message: " + message.getMessage())
        );

        // DB에서 메시지 검증
        List<Message> dbMessages = messageRepository.findTop100ByMessageRoom_RoomIdOrderBySentTimeAsc(room1.getRoomId());

        // 메시지 리스트가 null이 아니고 비어 있지 않은지 확인
        assertNotNull(dbMessages, "dbMessages list should not be null");
        assertFalse(dbMessages.isEmpty(), "No dbMessages found in Room '" + room1.getRoomId() + "'");

        assertEquals("First Message", dbMessages.get(0).getMessage());
        assertEquals("Second Message", dbMessages.get(1).getMessage());

        dbMessages.forEach(dbMessage ->
                System.out.println("Sender: " + dbMessage.getSender() + ", Message: " + dbMessage.getMessage())
        );
    }

    @Test
    void testLoadMessage_FromRedis() {
        MessageDto messageDto = MessageDto.builder()
                .sender(userA.getName())
                .roomId(room1.getRoomId())
                .message("First Message from Redis!")
                .build();

        messageService.saveMessage(messageDto);

        // 메시지 로드 테스트
        List<MessageDto> messages = messageService.loadMessage(room1.getRoomId());
        assertFalse(messages.isEmpty());
        assertEquals("First Message from Redis!", messages.get(0).getMessage());
    }

    @Test
    void testLoadMessage_FromDB() {
        MessageRoom messageRoom = messageRoomRepository.findByRoomId(room1.getRoomId())
                .orElseThrow(() -> new IllegalArgumentException("해당 쪽지방이 존재하지 않습니다."));
        Message message = Message.builder()
                .sender(userA.getName())
                .messageRoom(messageRoom)
                .message("First Message from DB!")
                .build();

        // DB에 메시지 저장
        messageRepository.save(message);

        // 메시지 로드 테스트
        List<MessageDto> messages = messageService.loadMessage(room1.getRoomId());
        assertFalse(messages.isEmpty());
        assertEquals("First Message from DB!", messages.get(0).getMessage());
    }
}
