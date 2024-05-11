package com.SafeNet.Backend.domain.post.domain;

import com.SafeNet.Backend.domain.likes.domain.Likes;
import com.SafeNet.Backend.domain.member.domain.Member;
import com.SafeNet.Backend.domain.messageRoom.domain.MessageRoom;
import com.SafeNet.Backend.domain.region.domain.Region;
import com.SafeNet.Backend.domain.files.domain.Files;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

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

    @Column(nullable = false, length = 100)
    private String title;

    @Column(nullable = false)
    private Integer cost;

    @Column(nullable = false)
    private Integer count;

    @Column(nullable = false)
    private LocalDate buyDate;

    @Lob
    private String contents;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(nullable = false)
    private LocalDateTime created;

    private LocalDateTime updated;

    @Builder.Default
    @Column(nullable = false)
    private Boolean status = false;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Category category;

    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member_post;

    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "region_id", nullable = false)
    private Region region_post;

    @OneToOne(fetch = LAZY, mappedBy = "post_msgroom", cascade = CascadeType.ALL)
    private MessageRoom messageRoom;

    @OneToMany(mappedBy = "post_likes", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    private List<Likes> likesList;

    @OneToMany(mappedBy = "post_files", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    private List<Files> filesList; // 단방향 참조
}
