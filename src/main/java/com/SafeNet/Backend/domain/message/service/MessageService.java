package com.SafeNet.Backend.domain.message.service;

import com.SafeNet.Backend.domain.message.entity.Message;
import com.SafeNet.Backend.domain.message.dto.MessageDto;
import com.SafeNet.Backend.domain.message.repository.MessageRepository;
import com.SafeNet.Backend.domain.messageroom.entity.MessageRoom;
import com.SafeNet.Backend.domain.messageroom.repository.MessageRoomRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
public class MessageService {

    private final RedisTemplate<String, MessageDto> redisTemplateMessage;
    private final MessageRepository messageRepository;
    private final MessageRoomRepository messageRoomRepository;

    public MessageService(
            @Qualifier("redisTemplateMessage") RedisTemplate<String, MessageDto> redisTemplateMessage,
            MessageRepository messageRepository,
            MessageRoomRepository messageRoomRepository) {
        this.redisTemplateMessage = redisTemplateMessage;
        this.messageRepository = messageRepository;
        this.messageRoomRepository = messageRoomRepository;
    }

    public void saveMessage(MessageDto messageDto) {
        MessageRoom messageRoom = messageRoomRepository.findByRoomId(messageDto.getRoomId())
                .orElseThrow(() -> new IllegalArgumentException("해당 쪽지방이 존재하지 않습니다."));

        if (!messageRoom.isFirstMessageSent()) {
            messageRoom.setFirstMessageSent(true);
            messageRoomRepository.save(messageRoom); // 첫 메시지 여부를 true로 업데이트
        }

        Message message = Message.builder()
                .sender(messageDto.getSender())
                .messageRoom(messageRoom)
                .message(messageDto.getMessage())
                .build();
        messageRepository.save(message);

        // Redis에 저장 (직렬화)
        redisTemplateMessage.setValueSerializer(new Jackson2JsonRedisSerializer<>(MessageDto.class));
        redisTemplateMessage.opsForList().rightPush(messageDto.getRoomId(), messageDto); // 직렬화된 데이터 저장
        redisTemplateMessage.expire(messageDto.getRoomId(), 24, TimeUnit.HOURS); // redis에서 24시간마다 삭제
    }

    /**
     * look aside 캐싱 전략 사용
     * 1. Redis에서 해당 채팅방의 최근 메시지 100개 가져온다.
     * 2. Redis에서 가져온 메시지가 없다면 DB에서 최근 메시지 100개 가져온다.
     * 3. DB에서 조회해 온 메시지들을 다시 Redis에 저장한다. (for문)
     * 4. Redis에 있다면 뒤쪽에 데이터를 붙인다.
     */
    public List<MessageDto> loadMessage(String roomId) {
        List<MessageDto> messageList = new ArrayList<>();

        // 1. Redis에서 데이터 가져오기 (역직렬화)
        List<MessageDto> redisMessageList = redisTemplateMessage.opsForList().range(roomId, 0, 99);

        // 2. Redis에 데이터가 없으면, DB에서 데이터 가져오기 및 Redis에 저장 (직렬화)
        if (redisMessageList == null || redisMessageList.isEmpty()) {
            List<Message> dbMessageList = messageRepository.findTop100ByMessageRoom_RoomIdOrderBySentTimeAsc(roomId);
            for (Message message : dbMessageList) {
                MessageDto messageDto = MessageDto.builder()
                        .sender(message.getSender())
                        .message(message.getMessage())
                        .roomId(roomId)
                        .sentTime(message.getSentTime().toString())
                        .build();
                messageList.add(messageDto);
                redisTemplateMessage.setValueSerializer(new Jackson2JsonRedisSerializer<>(MessageDto.class)); // 직렬화
                redisTemplateMessage.opsForList().rightPush(roomId, messageDto); // 직렬화된 데이터 저장
            }
            redisTemplateMessage.expire(roomId, 24, TimeUnit.HOURS); // 메시지 만료 시간 24시간 설정
        } else { // 4. Redis에서 읽어온 데이터 사용 (역직렬화), 뒤쪽에 데이터 붙이기
            messageList.addAll(redisMessageList);
            redisTemplateMessage.expire(roomId, 24, TimeUnit.HOURS); // 만료 시간 연장
        }
        return messageList;
    }
}
