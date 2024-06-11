package com.SafeNet.Backend.domain.messageroom.entity;

import com.SafeNet.Backend.domain.post.entity.Post;
import com.SafeNet.Backend.domain.member.entity.Member;
import com.SafeNet.Backend.domain.message.entity.Message;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static jakarta.persistence.FetchType.LAZY;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Getter
public class MessageRoom {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "message_room_id")
    private Long id;

    @Column(name = "room_id", unique = true, nullable = false)
    private String roomId;

    @Column(nullable = false)
    private String roomName;

    @Column(nullable = false)
    private String sender; // 채팅방 생성자(송신자)

    @Column(nullable = false)
    private String receiver; // 채팅방 수신자

    @Column(nullable = false)
    private boolean firstMessageSent = false; // 보낸 메시지가 존재하는지

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt; // 현재 시간 자동 할당

    @OneToMany(mappedBy = "messageRoom", cascade = CascadeType.REMOVE)
    private List<Message> messageList = new ArrayList<>();

    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "post_id", nullable = false)
    private Post post;

    public void setFirstMessageSent(boolean firstMessageSent) {
        this.firstMessageSent = firstMessageSent;
    }
}