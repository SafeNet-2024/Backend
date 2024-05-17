package com.SafeNet.Backend.domain.message.service;

import com.SafeNet.Backend.domain.message.entity.Message;
import com.SafeNet.Backend.domain.message.dto.MessageDto;
import com.SafeNet.Backend.domain.message.repository.MessageRepository;
import com.SafeNet.Backend.domain.messageroom.entity.MessageRoom;
import com.SafeNet.Backend.domain.messageroom.repository.MessageRoomRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class MessageService {
    private final RedisTemplate<String, MessageDto> redisTemplateMessage;
    private final MessageRepository messageRepository;
    private final MessageRoomRepository messageRoomRepository;

    public void saveMessage(MessageDto messageDto) {
        MessageRoom messageRoom = messageRoomRepository.findByRoomId(messageDto.getRoomId())
                .orElseThrow(() -> new IllegalArgumentException("해당 쪽지방이 존재하지 않습니다."));
        Message message = Message.builder()
                .sender(messageDto.getSender())
                .messageRoom(messageRoom)
                .message(messageDto.getMessage())
                .build();
        messageRepository.save(message);

        redisTemplateMessage.setValueSerializer(new Jackson2JsonRedisSerializer<>(MessageDto.class));
        redisTemplateMessage.opsForList().rightPush(messageDto.getRoomId(), messageDto);
        redisTemplateMessage.expire(messageDto.getRoomId(), 1, TimeUnit.HOURS); // redis에서 1시간마다 삭제
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

        // 1.
        List<MessageDto> redisMessageList = redisTemplateMessage.opsForList().range(roomId, 0, 99);

        // 2.
        if (redisMessageList == null || redisMessageList.isEmpty()) {
            List<Message> dbMessageList = messageRepository.findTop100ByMessageRoom_RoomIdOrderBySentTimeAsc(roomId);
            // 3.
            for (Message message : dbMessageList) {
                MessageDto messageDto = MessageDto.builder()
                        .sender(message.getSender())
                        .message(message.getMessage())
                        .roomId(roomId)
                        .sentTime(message.getSentTime().toString())
                        .build();
                messageList.add(messageDto);
                redisTemplateMessage.setValueSerializer(new Jackson2JsonRedisSerializer<>(Message.class));      // 직렬화
                redisTemplateMessage.opsForList().rightPush(roomId, messageDto);                                // redis 저장
            }
        } else { // 4. 뒤쪽에 데이터 붙이기
            messageList.addAll(redisMessageList);
        }
        return messageList;
    }
}
