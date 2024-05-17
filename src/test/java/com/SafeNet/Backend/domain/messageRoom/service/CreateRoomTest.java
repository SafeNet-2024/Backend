package com.SafeNet.Backend.domain.messageroom.service;

import com.SafeNet.Backend.domain.member.entity.Member;
import com.SafeNet.Backend.domain.member.repository.MemberRepository;
import com.SafeNet.Backend.domain.message.dto.MessageRequestDto;
import com.SafeNet.Backend.domain.message.dto.MessageResponseDto;
import com.SafeNet.Backend.domain.messageroom.dto.MessageRoomDto;
import com.SafeNet.Backend.domain.messageroom.repository.MessageRoomRepository;
import com.SafeNet.Backend.domain.post.entity.Post;
import com.SafeNet.Backend.domain.post.repository.PostRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
class CreateRoomTest {

    @Autowired
    private MessageRoomService messageRoomService;

    @Autowired
    private MessageRoomRepository messageRoomRepository;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @AfterEach
    void tearDown() {
        // 테스트가 끝난 후 Redis 데이터 정리
        redisTemplate.delete("MESSAGE_ROOM"); // 테스트가 끝난 후에도 Redis 채팅방 데이터 삭제
    }

    @Test
    void createRoom() {
        // 사용자와 게시물 미리 설정
        Member user = Member.builder().name("userA").build();
        memberRepository.save(user);

        Post post = Post.builder().member(user).build();
        postRepository.save(post);

        // 새 쪽지방 요청
        MessageRequestDto requestDto = MessageRequestDto.builder().receiver("userB").postId(post.getId()).build();

        // 실행
        MessageResponseDto result = messageRoomService.createRoom(requestDto, user);

        // 검증
        assertThat(result.getReceiver()).isEqualTo(requestDto.getReceiver());

        HashOperations<String, String, MessageRoomDto> hashOps = redisTemplate.opsForHash();
        Map<String, MessageRoomDto> rooms = hashOps.entries("MESSAGE_ROOM");

        // 출력하여 확인
        rooms.forEach((key, value) -> {
            System.out.println("Key: " + key);
            System.out.println("Room ID: " + value.getRoomId());
            System.out.println("ID: " + value.getId());
            System.out.println("Room Name: " + value.getRoomName());
            System.out.println("Sender: " + value.getSender());
            System.out.println("Receiver: " + value.getReceiver());
        });

        // 여러 채팅방이 존재하는지 검증
        assertThat(rooms).isNotEmpty();
    }
}
