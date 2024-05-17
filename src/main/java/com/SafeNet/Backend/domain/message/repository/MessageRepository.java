package com.SafeNet.Backend.domain.message.repository;

import com.SafeNet.Backend.domain.message.domain.Message;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MessageRepository extends JpaRepository<Message, Long> {
    // 특정 채팅방(roomId로 식별)에서 생성된 메시지들 중 최신 100개를 오래된 순서로 조회
    // 초기에 로딩시 사용
    List<Message> findTop100ByMessageRoom_RoomIdOrderBySentTimeAsc(String roomId);


    // 특정 채팅방에서 가장 최근에 생성된 메시지 하나를 조회
    // 사용자의 채팅방 목록에서 각 채팅방의 마지막 메시지를 보여주기 위해 사용
    Message findTopByMessageRoom_RoomIdOrderBySentTimeDesc(String roomId);
}
