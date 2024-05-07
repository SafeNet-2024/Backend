package com.SafeNet.Backend.domain.post.domain;

import com.SafeNet.Backend.domain.member.domain.Member;
import com.SafeNet.Backend.domain.messageRoom.domain.MessageRoom;
import jakarta.persistence.*;
import lombok.*;

import static jakarta.persistence.FetchType.LAZY;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Getter
public class Post {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "post_id")
    private Long id;

    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @OneToOne(fetch = LAZY, mappedBy = "post", cascade = CascadeType.ALL)
    private MessageRoom messageRoom;
}
