package com.SafeNet.Backend.domain.messageroom.service;

import com.SafeNet.Backend.domain.member.entity.Member;
import com.SafeNet.Backend.domain.member.repository.MemberRepository;
import com.SafeNet.Backend.domain.message.dto.MessageDto;
import com.SafeNet.Backend.domain.message.entity.Message;
import com.SafeNet.Backend.domain.message.dto.MessageResponseDto;
import com.SafeNet.Backend.domain.message.repository.MessageRepository;
import com.SafeNet.Backend.domain.message.service.MessageService;
import com.SafeNet.Backend.domain.messageroom.entity.MessageRoom;
import com.SafeNet.Backend.domain.messageroom.dto.MessageRoomDto;
import com.SafeNet.Backend.domain.messageroom.repository.MessageRoomRepository;
import com.SafeNet.Backend.domain.post.entity.Post;
import com.SafeNet.Backend.domain.post.exception.PostException;
import com.SafeNet.Backend.domain.post.repository.PostRepository;
import com.SafeNet.Backend.global.exception.CustomException;
import com.SafeNet.Backend.global.pubsub.RedisPublisher;
import com.SafeNet.Backend.global.pubsub.RedisSubscriber;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;

@Slf4j
@Service
public class MessageRoomService {
    private final MessageRoomRepository messageRoomRepository;
    private final MessageRepository messageRepository;
    private final PostRepository postRepository;
    private final MemberRepository memberRepository;
    private final RedisMessageListenerContainer redisMessageListener;
    private final RedisSubscriber redisSubscriber;
    private final RedisTemplate<String, Object> redisTemplate;

    private HashOperations<String, String, MessageRoomDto> opsHashMessageRoom;
    private Map<String, ChannelTopic> topics;
    private final RedisPublisher redisPublisher; // RedisPublisher 주입

    private final MessageService messageService;

    private static final String Message_Rooms = "MESSAGE_ROOM"; // Redis에 채팅방 데이터를 저장하기 위한 해시맵의 키

    public MessageRoomService(
            MessageRoomRepository messageRoomRepository,
            MessageRepository messageRepository,
            PostRepository postRepository,
            MemberRepository memberRepository,
            RedisMessageListenerContainer redisMessageListener,
            RedisSubscriber redisSubscriber,
            @Qualifier("customRedisTemplate") RedisTemplate<String, Object> redisTemplate,
            RedisPublisher redisPublisher,
            MessageService messageService) {
        this.messageRoomRepository = messageRoomRepository;
        this.messageRepository = messageRepository;
        this.postRepository = postRepository;
        this.memberRepository = memberRepository;
        this.redisMessageListener = redisMessageListener;
        this.redisSubscriber = redisSubscriber;
        this.redisTemplate = redisTemplate;
        this.redisPublisher = redisPublisher;
        this.messageService = messageService;
    }

    @PostConstruct
    private void init() {
        opsHashMessageRoom = redisTemplate.opsForHash();
        topics = new HashMap<>();
    }

    // 1:1 채팅방 생성
    public MessageResponseDto createRoom(Long postId, String email) {
        Member member = memberRepository.findByEmail(email).orElseThrow(() -> new PostException("Member not found with email: " + email, HttpStatus.NOT_FOUND));
        Post post = postRepository.findById(postId).orElseThrow(() -> new PostException("Post not found with id: " + postId, HttpStatus.NOT_FOUND));

        if (post.getMember().getEmail().equals(email)) {
            throw new PostException("You cannot create a chat room for your own post", HttpStatus.BAD_REQUEST); // 게시물 작성자인 경우 채팅방을 생성하지 못하도록 예외 처리
        }

        String receiver = post.getMember().getName(); // 게시물의 작성자를 receiver로 설정

        // sender와 receiver가 속해있는 채팅방 조회
        Optional<MessageRoom> messageRoom = messageRoomRepository.findBySenderAndReceiverAndPostId(member.getName(), receiver, postId);
        try {
            // 처음 쪽지방 생성한 경우와 해당 게시물에 이미 생성된 쪽지방이 아닌 경우
            // 한 게시글에 하나의 채팅방만 생성되도록 구현
            if (messageRoom.isEmpty()) {
                MessageRoomDto messageRoomDto = MessageRoomDto.createMessageRoom(postId, member, receiver);
                // redis 저장
                opsHashMessageRoom.put(Message_Rooms, messageRoomDto.getRoomId(), messageRoomDto);
                // db 저장
                MessageRoom newRoom = MessageRoom.builder()
                        .id(messageRoomDto.getId())
                        .roomName(messageRoomDto.getRoomName())
                        .roomId(messageRoomDto.getRoomId())
                        .sender(messageRoomDto.getSender())
                        .receiver(messageRoomDto.getReceiver())
                        .member(member)
                        .post(post)
                        .build();
                MessageRoom new_messageRoom = messageRoomRepository.save(newRoom);

                return MessageResponseDto.builder()
                        .id(new_messageRoom.getId())
                        .roomName(new_messageRoom.getRoomName())
                        .sender(new_messageRoom.getSender())
                        .roomId(new_messageRoom.getRoomId())
                        .receiver(new_messageRoom.getReceiver())
                        .postId(messageRoomDto.getPostId()).build();

            } else { // 이미 생성된 채팅방인 경우 중복 생성되지 않게 해당 채팅방으로 이동
                MessageRoom existingRoom = messageRoom.get();
                return MessageResponseDto.builder()
                        .roomId(existingRoom.getRoomId()).build();
            }
        } catch (Exception ex) {
            throw new CustomException("채팅방을 생성하던 도중: " + ex.getMessage());
        }
    }


    /**
     * 사용자 관련 쪽지방 전체 조회
     * 채팅방 이름은 상대방 이름으로 기본 설정됨
     * user가 sender인 경우 해당 채팅방의 이름이 receiver이고
     * user가 receiver인 경우 해당 채팅방의 이름이 sender이다.
     */
    public List<MessageResponseDto> findAllRoomByUser(String email) {
        Member member = memberRepository.findByEmail(email).orElseThrow(() -> new PostException("Member not found with email: " + email, HttpStatus.NOT_FOUND));
        List<MessageRoom> messageRooms = messageRoomRepository.findByMemberOrReceiver(member, member.getName());
        List<MessageResponseDto> messageRoomDtos = new ArrayList<>();

        for (MessageRoom messageRoom : messageRooms) {
            if (member.getName().equals(messageRoom.getSender())) { //  member가 sender인 경우
                // 가장 최신 메시지 & 생성 시간 조회
                // TimeStamped 클래스에서 설정해둔 보낸 시간(sentTime)을 통해 각 채팅방에서 가장 최근 메시지와 그 메시지가 보내진 시간을 꺼낸다.
                Message latestMessage = messageRepository.findTopByMessageRoom_RoomIdOrderBySentTimeDesc(messageRoom.getRoomId());
                MessageResponseDto messageRoomDto = setLatestMessage(messageRoom, latestMessage, messageRoom.getReceiver());
                messageRoomDtos.add(messageRoomDto);

            } else if (member.getName().equals(messageRoom.getReceiver()) || messageRoom.isFirstMessageSent()) {  // user가 receiver이면서 첫번째 메시지가 있는 경우
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
    public MessageRoomDto findRoom(String roomId, String email) {
        Member member = memberRepository.findByEmail(email).orElseThrow(() -> new PostException("Member not found with email: " + email, HttpStatus.NOT_FOUND));

        MessageRoom messageRoom = messageRoomRepository.findByRoomId(roomId)
                .orElseThrow(() -> new IllegalArgumentException("The chat room does not exist."));

        // 게시글 검증 없이 메시지룸의 post 참조
        Post post = messageRoom.getPost();
        if (post == null) {
            throw new IllegalArgumentException("The chat room is not connected to any post.");
        }

        // sender 또는 receiver 확인
        if (!messageRoom.getSender().equals(member.getName()) && !messageRoom.getReceiver().equals(member.getName())) {
            throw new IllegalArgumentException("You do not have permission to access this chat room.");
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
    public MessageResponseDto deleteRoom(String roomId, String email) {
        Member member = memberRepository.findByEmail(email).orElseThrow(() -> new PostException("Member not found with email: " + email, HttpStatus.NOT_FOUND));
        MessageRoom messageRoom = messageRoomRepository.findByRoomIdAndMemberOrRoomIdAndReceiver(roomId, member, roomId, member.getName());
        boolean deleteFromDb = false;
        String updatedSender = messageRoom.getSender();
        String updatedReceiver = messageRoom.getReceiver();

        // sender가 삭제하는 경우
        if (member.getName().equals(messageRoom.getSender())) {
            log.info("messageRoom.getSender()" + messageRoom.getSender());
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
                    .message("Both the receiver and the sender have left the room.")
                    .status(HttpStatus.OK.value())
                    .build();
        } else {
            MessageRoom updatedRoom = MessageRoom.builder()
                    .id(messageRoom.getId())
                    .roomName(updatedReceiver)
                    .roomId(messageRoom.getRoomId())
                    .sender(updatedSender)
                    .receiver(updatedReceiver)
                    .member(messageRoom.getMember())
                    .post(messageRoom.getPost())
                    .createdAt(messageRoom.getCreatedAt())
                    .build();

            messageRoomRepository.save(updatedRoom);
            MessageRoomDto messageRoomDto = convertToDto(messageRoom);
            opsHashMessageRoom.put(Message_Rooms, messageRoomDto.getRoomId(), messageRoomDto); // Redis 업데이트

            return MessageResponseDto.builder()
                    .message("The other person has left the room, so further chatting is not possible.")
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
    public void enterMessageRoom(String roomId, String username) {
        ChannelTopic topic = topics.get(roomId);

        if (topic == null) {
            topic = new ChannelTopic(roomId);
            redisMessageListener.addMessageListener(redisSubscriber, topic);
            topics.put(roomId, topic);

            // 입장 메시지 추가 (채팅방에 처음 입장하는 경우)
            MessageDto enterMessage = MessageDto.builder()
                    .sender(username) // 메시지 보낸 사람으로 username 설정
                    .roomId(roomId)
                    .message(username + "이(가) 채팅방에 입장했습니다")
                    .sentTime(LocalDateTime.now().toString())
                    .build();
            redisPublisher.publish(topic, enterMessage);
        }
    }

    // redis 채널에서 쪽지방 조회
    public ChannelTopic getTopic(String roomId) {
        return topics.get(roomId);
    }

    public void handleMessage(String roomId, String username, MessageDto messageDto) {
        // 클라이언트 채팅방(topic) 입장, 대화를 위해 리스너와 연동
        enterMessageRoom(roomId, username);

        // 메시지 전송 로직
        MessageDto messageWithTime = MessageDto.builder()
                .sender(messageDto.getSender())
                .roomId(messageDto.getRoomId())
                .message(messageDto.getMessage())
                .build();

        // Websocket에 발행된 메시지를 redis로 발행한다(publish)
        // 해당 쪽지방을 구독(subscribe)한 클라이언트에게 메시지가 실시간 전송
        redisPublisher.publish(getTopic(messageDto.getRoomId()), messageWithTime);

        // DB와 Redis에 메시지 저장
        messageService.saveMessage(messageDto);
    }
}
