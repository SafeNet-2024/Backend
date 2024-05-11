package com.SafeNet.Backend.domain.messageRoom.service;

import com.SafeNet.Backend.domain.member.domain.Member;
import com.SafeNet.Backend.domain.member.repository.MemberRepository;
import com.SafeNet.Backend.domain.message.domain.Message;
import com.SafeNet.Backend.domain.message.dto.MessageRequestDto;
import com.SafeNet.Backend.domain.message.dto.MessageResponseDto;
import com.SafeNet.Backend.domain.message.repository.MessageRepository;
import com.SafeNet.Backend.domain.messageRoom.domain.MessageRoom;
import com.SafeNet.Backend.domain.messageRoom.dto.MessageRoomDto;
import com.SafeNet.Backend.domain.messageRoom.repository.MessageRoomRepository;
import com.SafeNet.Backend.domain.post.domain.Post;
import com.SafeNet.Backend.domain.post.repository.PostRepository;
import com.SafeNet.Backend.global.pubsub.RedisSubscriber;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class MessageRoomService {
    private final MessageRoomRepository messageRoomRepository;
    private final MessageRepository messageRepository;
    private final PostRepository postRepository;
    private final MemberRepository memberRepository;

    // 쪽지방(topic)에 발행되는 메시지를 처리하는 리스너
    private final RedisMessageListenerContainer redisMessageListener;

    // 구독 처리 서비스
    private final RedisSubscriber redisSubscriber;

    private static final String Message_Rooms = "MESSAGE_ROOM"; // Redis에 채팅방 데이터를 저장하기 위한 해시맵의 키(채팅방 데이터를 Redis에서 조회 및 저장)

    private final RedisTemplate<String, Object> redisTemplate; // Redis와 상호 작용하기 위한 RedisTemplate
    private HashOperations<String, String, MessageRoomDto> opsHashMessageRoom; // RedisTemplate을 사용하여 Redis 해시(Hash) 데이터 구조에 접근하기 위한 HashOperations

    // 채팅방의 대화 메시지 발행을 위한 redis topic(채팅방) 정보
    // 서버별로 쪽지방에 매치되는 topic 정보를 Map에 넣고, 이는 roomId로 찾는다.
    private Map<String, ChannelTopic> topics;

    @PostConstruct
    private void init() {
        opsHashMessageRoom = redisTemplate.opsForHash();
        topics = new HashMap<>(); // HashMap 객체를 초기화하여 topics 필드를 초기화
    }

    // 1:1 채팅방 생성
    public MessageResponseDto createRoom(MessageRequestDto messageRequestDto, Member member) {
        Post post = postRepository.findById(messageRequestDto.getPostId()).orElseThrow(
                () -> new IllegalArgumentException("게시글을 찾을 수 없습니다.")
        );

        // sender와 receiver가 속해있는 채팅방 조회
        MessageRoom messageRoom = messageRoomRepository.findBySenderAndReceiver(member.getName(), messageRequestDto.getReceiver());

        // 처음 쪽지방 생성한 경우와 해당 게시물에 이미 생성된 쪽지방이 아닌 경우
        // 한 게시글에 하나의 채팅방만 생성되도록 구현
        if (messageRoom == null ||
                !member.getName().equals(messageRoom.getSender()) &&
                        !messageRequestDto.getReceiver().equals(messageRoom.getReceiver()) &&
                        !messageRequestDto.getPostId().equals(post.getId())) {

            MessageRoomDto messageRoomDto = MessageRoomDto.createMessageRoom(messageRequestDto, member);
            // redis 저장
            opsHashMessageRoom.put(Message_Rooms, messageRoomDto.getRoomId(), messageRoomDto);
            // db 저장
            MessageRoom newRoom = MessageRoom.builder()
                    .id(messageRoomDto.getId())
                    .roomName(messageRoomDto.getRoomName())
                    .roomId(messageRoomDto.getRoomId())
                    .sender(messageRoomDto.getSender())
                    .receiver(messageRoomDto.getReceiver())
                    .member_msgroom(member)
                    .post_msgroom(post)
                    .build();
            messageRoom = messageRoomRepository.save(newRoom);

            return MessageResponseDto.builder()
                    .id(messageRoom.getId())
                    .roomName(messageRoom.getRoomName())
                    .sender(messageRoom.getSender())
                    .roomId(messageRoom.getRoomId())
                    .receiver(messageRoom.getReceiver())
                    .postId(messageRoomDto.getPostId()).build();

        } else { // 이미 생성된 채팅방인 경우 중복 생성되지 않게 해당 채팅방으로 이동
            return MessageResponseDto.builder()
                    .roomId(messageRoom.getRoomId()).build();
        }
    }


    /**
     * 사용자 관련 쪽지방 전체 조회
     * 채팅방 이름은 상대방 이름으로 기본 설정됨
     * user가 sender인 경우 해당 채팅방의 이름이 receiver이고
     * user가 receiver인 경우 해당 채팅방의 이름이 sender이다.
     */
    public List<MessageResponseDto> findAllRoomByUser(Member member) {
        List<MessageRoom> messageRooms = messageRoomRepository.findByMemberOrReceiver(member, member.getName());
        List<MessageResponseDto> messageRoomDtos = new ArrayList<>();

        for (MessageRoom messageRoom : messageRooms) {
            //  member가 sender인 경우
            if (member.getName().equals(messageRoom.getSender())) {
                // 가장 최신 메시지 & 생성 시간 조회
                // TimeStamped 클래스에서 설정해둔 보낸 시간(sentTime)을 통해 각 채팅방에서 가장 최근 메시지와 그 메시지가 보내진 시간을 꺼낸다.
                Message latestMessage = messageRepository.findTopByMessageRoom_RoomIdOrderBySentTimeDesc(messageRoom.getRoomId());
                MessageResponseDto messageRoomDto = setLatestMessage(messageRoom, latestMessage, messageRoom.getReceiver());
                messageRoomDtos.add(messageRoomDto);

            } else {  // user가 receiver인 경우
                // 가장 최신 메시지 & 생성 시간 조회
                Message latestMessage = messageRepository.findTopByMessageRoom_RoomIdOrderBySentTimeDesc(messageRoom.getRoomId());
                MessageResponseDto messageRoomDto = setLatestMessage(messageRoom, latestMessage, messageRoom.getSender());
                messageRoomDtos.add(messageRoomDto);
            }
        }

        return messageRoomDtos;
    }

    private static MessageResponseDto setLatestMessage(MessageRoom messageRoom, Message latestMessage, String name) {
        MessageResponseDto messageRoomDto;
        if (latestMessage != null) {
            messageRoomDto = MessageResponseDto.builder()
                    .id(messageRoom.getId())
                    .roomName(name)
                    .roomId(messageRoom.getRoomId())
                    .sender(messageRoom.getSender())
                    .receiver(messageRoom.getReceiver())
                    .createdAt(latestMessage.getSentTime())
                    .message(latestMessage.getMessage()).build();
        } else {
            messageRoomDto = MessageResponseDto.builder()
                    .id(messageRoom.getId())
                    .roomName(name)
                    .roomId(messageRoom.getRoomId())
                    .sender(messageRoom.getSender())
                    .receiver(messageRoom.getReceiver())
                    .createdAt(messageRoom.getCreatedAt()).build();
        }
        return messageRoomDto;
    }

    /**
     * 사용자 관련 쪽지방 선택 조회
     * sender와 receiver만 해당 쪽지방을 조회할 수 있다.
     */
    public MessageRoomDto findRoom(String roomId, Member member) {
        MessageRoom messageRoom = messageRoomRepository.findByRoomId(roomId)
                .orElseThrow(() -> new IllegalArgumentException("해당 쪽지방이 존재하지 않습니다."));

        // 게시글 검증 없이 메시지룸의 post 참조
        Post post = messageRoom.getPost_msgroom();
        if (post == null) {
            throw new IllegalArgumentException("쪽지방에 연결된 게시물이 존재하지 않습니다.");
        }

        // sender 또는 receiver 확인
        if (!messageRoom.getSender().equals(member.getName()) && !messageRoom.getReceiver().equals(member.getName())) {
            throw new IllegalArgumentException("해당 쪽지방에 접근할 권한이 없습니다.");
        }

        return MessageRoomDto.builder()
                .id(messageRoom.getId())
                .roomName(messageRoom.getRoomName())
                .roomId(messageRoom.getRoomId())
                .sender(messageRoom.getSender())
                .receiver(messageRoom.getReceiver())
                .postId(post.getId()).build();
    }


    /**
     * 쪽지방 삭제
     * sender와 receiver가 삭제할 경우 Redis 및 DB 모두 삭제
     * sender나 receiver가 삭제할 경우 sender나 receiver가 "Not_Exist_Receiver(Sender)"로 변경됨
     * member는 더이상 해당 채팅방을 조회하지 못함.
     */
    public MessageResponseDto deleteRoom(String roomId, Member member) {
        MessageRoom messageRoom = messageRoomRepository.findByRoomIdAndMemberOrRoomIdAndReceiver(roomId, member, roomId, member.getName());
        boolean deleteFromDb = false;
        String updatedSender = messageRoom.getSender();
        String updatedReceiver = messageRoom.getReceiver();

        // sender가 삭제하는 경우
        if (member.getName().equals(messageRoom.getSender())) {
            updatedSender = "Not_Exist_Sender";
            deleteFromDb = "Not_Exist_Receiver".equals(messageRoom.getReceiver()); // receiver도 이미 삭제했는지 확인
        }
        // receiver가 삭제하는 경우
        else if (member.getName().equals(messageRoom.getReceiver())) {
            updatedReceiver = "Not_Exist_Receiver";
            deleteFromDb = "Not_Exist_Sender".equals(messageRoom.getSender()); // sender도 이미 삭제했는지 확인
        }

        // sender와 receiver 모두 삭제했다면 DB와 Redis에서 완전히 삭제
        if (deleteFromDb) {
            messageRoomRepository.delete(messageRoom);
            opsHashMessageRoom.delete(Message_Rooms, messageRoom.getRoomId());
            return MessageResponseDto.builder()
                    .message("receiver와 sender가 모두 방을 나갔습니다.")
                    .status(HttpStatus.OK.value())
                    .build();
        } else {
            MessageRoom updatedRoom = MessageRoom.builder()
                    .id(messageRoom.getId())
                    .roomName(updatedReceiver)
                    .roomId(messageRoom.getRoomId())
                    .sender(updatedSender)
                    .receiver(updatedReceiver)
                    .member_msgroom(messageRoom.getMember_msgroom())
                    .post_msgroom(messageRoom.getPost_msgroom())
                    .createdAt(messageRoom.getCreatedAt())
                    .build();

            messageRoomRepository.save(updatedRoom);
            MessageRoomDto messageRoomDto = convertToDto(messageRoom);
            opsHashMessageRoom.put(Message_Rooms, messageRoomDto.getRoomId(), messageRoomDto); // Redis 업데이트

            return MessageResponseDto.builder()
                    .message("상대방이 방을 나갔으므로 더 이상 채팅이 불가능합니다.")
                    .status(HttpStatus.OK.value())
                    .build();
        }
    }

    private MessageRoomDto convertToDto(MessageRoom messageRoom) {
        return MessageRoomDto.builder()
                .id(messageRoom.getId())
                .roomName(messageRoom.getRoomName())
                .roomId(messageRoom.getRoomId())
                .sender(messageRoom.getSender())
                .receiver(messageRoom.getReceiver())
                .build();
    }

    // 쪽지방 입장
    public void enterMessageRoom(String roomId) {
        ChannelTopic topic = topics.get(roomId);

        if (topic == null) {
            topic = new ChannelTopic(roomId);
            redisMessageListener.addMessageListener(redisSubscriber, topic);
            topics.put(roomId, topic);
        }
    }

    // redis 채널에서 쪽지방 조회
    public ChannelTopic getTopic(String roomId) {
        return topics.get(roomId);
    }
}
