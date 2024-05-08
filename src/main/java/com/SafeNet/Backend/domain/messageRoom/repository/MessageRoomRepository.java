package com.SafeNet.Backend.domain.messageRoom.repository;


import com.SafeNet.Backend.domain.member.domain.Member;
import com.SafeNet.Backend.domain.messageRoom.domain.MessageRoom;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MessageRoomRepository extends JpaRepository<MessageRoom, Long> {

    // 주어진 Member 객체나 수신자의 이름을 기반으로 채팅룸 조회
    // 채팅방 리스트: 한 명의 사용자가 참여한 모든 채팅방을 조회할 때 사용
    List<MessageRoom> findByMemberOrReceiver(Member member, String receiver);

    // 특정 ID와 사용자 또는 ID와 수신자 이름을 조건으로 채팅룸 조회
    // 채팅방 삭제: 채팅방 삭제시 사용
    MessageRoom findByIdAndMemberOrIdAndReceiver(Long id, Member member, Long id1, String nickname);

    // 특정 발신자와 수신자를 조건으로 하여 메시지룸 조회
    // 채팅방 조회: 특정 두 사용자 사이의 채팅방을 찾을 때 사용
    MessageRoom findBySenderAndReceiver(String nickname, String receiver);

    // 채팅방 ID와 사용자 정보 또는 채팅방 ID와 수신자의 이름을 조건으로 메시지룸 조회
    // 채팅방 조회: 특정 채팅방에 접근할 권한이 있는지 확인할 때 사용
    MessageRoom findByRoomIdAndMemberOrRoomIdAndReceiver(String roomId, Member member, String roomId1, String nickname);

    // 채팅방의 ID로 채팅룸을 단순 조회
    MessageRoom findByRoomId(String roomId);
}
