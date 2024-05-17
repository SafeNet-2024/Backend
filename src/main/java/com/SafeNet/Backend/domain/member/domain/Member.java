package com.SafeNet.Backend.domain.member.domain;

import com.SafeNet.Backend.domain.file.domain.File;
import com.SafeNet.Backend.domain.postLike.domain.PostLike;
import com.SafeNet.Backend.domain.messageroom.domain.MessageRoom;
import com.SafeNet.Backend.domain.post.domain.Post;
import com.SafeNet.Backend.domain.region.domain.Region;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.util.List;

import static jakarta.persistence.FetchType.LAZY;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Getter
public class Member {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "member_id")
    private Long id;

    @Column(nullable = false, unique = true, length = 100)
    private String name;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(length = 15)
    private String phoneNumber;

    @Column(nullable = false)
    private String pwd;

    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "region_id", nullable = false)
    private Region region;

    @OneToMany(mappedBy = "member", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    private List<MessageRoom> messageRooms;

    @OneToMany(mappedBy = "member", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    private List<Post> postList;

    @OneToMany(mappedBy = "member", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    private List<PostLike> postLikeList;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "member_id")
    @JsonIgnore
    private List<File> fileList; // 단방향 참조 (File 클래스에서 Member에 대한 참조 안함)
}
