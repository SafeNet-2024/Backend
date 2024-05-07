package com.SafeNet.Backend.domain.message.domain;


import com.SafeNet.Backend.domain.messageRoom.domain.MessageRoom;
import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

import static jakarta.persistence.FetchType.LAZY;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Getter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Message {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "message_id")
    private Long id;

    private String sender;

    private String receiver;

    private String message;

    @CreationTimestamp
    @Column(name = "sent_time", nullable = false, updatable = false)
    private LocalDateTime sentTime;

    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "room_id", referencedColumnName = "room_id")
    private MessageRoom messageRoom;
}
