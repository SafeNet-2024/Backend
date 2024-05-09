package com.SafeNet.Backend.domain.messageRoom.service;

import com.SafeNet.Backend.domain.member.domain.Member;
import com.SafeNet.Backend.domain.member.repository.MemberRepository;
import com.SafeNet.Backend.domain.message.dto.MessageRequestDto;
import com.SafeNet.Backend.domain.message.dto.MessageResponseDto;
import com.SafeNet.Backend.domain.message.repository.MessageRepository;
import com.SafeNet.Backend.domain.messageRoom.domain.MessageRoom;
import com.SafeNet.Backend.domain.messageRoom.dto.MessageRoomDto;
import com.SafeNet.Backend.domain.messageRoom.repository.MessageRoomRepository;
import com.SafeNet.Backend.domain.post.domain.Post;
import com.SafeNet.Backend.domain.post.repository.PostRepository;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.*;

@SpringBootTest
@Transactional
class MessageRoomServiceTest {
    @Autowired
    private MessageRoomService messageRoomService;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private MessageRoomRepository messageRoomRepository;

    @Autowired
    private MessageRepository messageRepository;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Autowired
    private PostRepository postRepository;

    private Member userA;
    private Member userB;
    private MessageResponseDto room1;
    private MessageResponseDto room2;


    @BeforeEach
    void setUp() {
        redisTemplate.delete("MESSAGE_ROOM"); // 테스트 실행 전 Redis 채팅방 데이터 삭제

        userA = memberRepository.save(Member.builder().name("userA").build());
        userB = memberRepository.save(Member.builder().name("userB").build());

        Post post1 = postRepository.save(Post.builder().member(userA).build());
        Post post2 = postRepository.save(Post.builder().member(userB).build());

        MessageRequestDto requestDto1 = MessageRequestDto.builder().receiver("userB").postId(post1.getId()).build();
        MessageRequestDto requestDto2 = MessageRequestDto.builder().receiver("userA").postId(post2.getId()).build();

        room1 = messageRoomService.createRoom(requestDto1, userA); // userA -> userB
        room2 = messageRoomService.createRoom(requestDto2, userB); // userA -> userB
    }

    @AfterEach
    void tearDown() {
        // 테스트가 끝난 후 Redis 데이터 정리
        redisTemplate.delete("MESSAGE_ROOM");
    }

    @Test
    void findAllRoomByUser() { // user가 sender인 경우
        List<MessageResponseDto> results = messageRoomService.findAllRoomByUser(userA);
        assertThat(results).hasSize(2);
        assertThat(results.get(0).getSender()).isEqualTo(userA.getName());
        assertThat(results.get(0).getReceiver()).isEqualTo(userB.getName());

        checkRedis();
    }

    @Test
    void findRoom_ShouldReturnRoomDetails_WhenUserHasAccess() {
        MessageRoomDto foundRoom = messageRoomService.findRoom(room1.getRoomId(), userA);
        assertThat(foundRoom.getRoomId()).isEqualTo(room1.getRoomId());
        assertThat(foundRoom.getSender()).isEqualTo(userA.getName());
        assertThat(foundRoom.getReceiver()).isEqualTo("userB");
        checkRedis();
    }

    @Test
    void findRoom_ShouldThrowException_WhenUserHasNoAccess() {
        Member otherUser1 = Member.builder().name("otherUser").build();
        Member otherUser = memberRepository.save(otherUser1); // 추가된 다른 사용자 초기화
        assertThrows(IllegalArgumentException.class, () -> {
            messageRoomService.findRoom(room1.getRoomId(), otherUser);
        });
    }

    @Test
    void deleteRoom() {
        checkRedis();
        // userA deletes the room first
        messageRoomService.deleteRoom(room1.getRoomId(), userA);
        // Check if the room is updated and not deleted after userA's deletion
        MessageRoom updatedRoom = messageRoomRepository.findById(room1.getId()).orElse(null);
        assertNotNull(updatedRoom);
        assertThat(updatedRoom.getSender()).isEqualTo("Not_Exist_Sender");
        HashOperations<String, String, MessageRoomDto> hashOps = redisTemplate.opsForHash();
        Map<String, MessageRoomDto> rooms = hashOps.entries("MESSAGE_ROOM");
        assertThat(rooms.containsKey(room1.getRoomId())).isTrue();
        checkRedis();

        // Then userB deletes the room
        messageRoomService.deleteRoom(room1.getRoomId(), userB);
        // Check if the room is deleted from the database and Redis
        assertThat(messageRoomRepository.findById(room1.getId())).isEmpty();

        Map<String, MessageRoomDto> rooms_after = hashOps.entries("MESSAGE_ROOM");
        assertThat(rooms_after.containsKey(room1.getRoomId())).isFalse();
        checkRedis();
    }

    @Test
    void enterMessageRoom() {
        //TODO 프론트와 연결 후 추후에 테스트 진행
        messageRoomService.enterMessageRoom(room1.getRoomId());
        ChannelTopic topic = messageRoomService.getTopic(room1.getRoomId());

        assertNotNull(topic, "Topic should not be null after entering room");
        assertEquals(room1.getRoomId(), topic.getTopic(), "Topic name should match the room ID");
    }

    private void checkRedis() {
        HashOperations<String, String, MessageRoomDto> hashOps = redisTemplate.opsForHash();
        Map<String, MessageRoomDto> rooms = hashOps.entries("MESSAGE_ROOM");

        // 출력하여 확인
        rooms.forEach((key, value) -> {
            System.out.println("====================================");
            System.out.println("Key: " + key);
            System.out.println("Room ID: " + value.getRoomId());
            System.out.println("ID: " + value.getId());
            System.out.println("Room Name: " + value.getRoomName());
            System.out.println("Sender: " + value.getSender());
            System.out.println("Receiver: " + value.getReceiver());
            System.out.println("====================================");
        });
        assertThat(rooms).isNotEmpty();
    }
}
