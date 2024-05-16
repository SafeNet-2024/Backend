package com.SafeNet.Backend.domain.post.domain;

import com.SafeNet.Backend.domain.post.dto.PostRequestDto;
import com.SafeNet.Backend.domain.postLike.domain.PostLike;
import com.SafeNet.Backend.domain.member.domain.Member;
import com.SafeNet.Backend.domain.messageroom.domain.MessageRoom;
import com.SafeNet.Backend.domain.region.domain.Region;
import com.SafeNet.Backend.domain.file.domain.File;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static jakarta.persistence.FetchType.LAZY;

@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
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
    private int cost;

    @Column(nullable = false)
    private int count;

    @Column(nullable = false)
    private LocalDate buyDate;

    @Lob
    private String contents;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime created;

    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime updated;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    @Column(nullable = false)
    private PostStatus status = PostStatus.AVAILABLE; // 기본값: 거래가능

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Category category;

    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "region_id", nullable = false)
    private Region region;

    @OneToOne(fetch = LAZY, mappedBy = "post", cascade = CascadeType.ALL)
    private MessageRoom messageRoom;

    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    private List<PostLike> postLikeList;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "post_id")
    @JsonIgnore
    private List<File> fileList; // 단방향

    // 업데이트 메서드
    public Post updatePost(PostRequestDto dto, LocalDate parsedBuyDate, List<File> fileList) {
        return this.toBuilder()
                .title(dto.getTitle())
                .category(dto.getCategory())
                .cost(dto.getCost())
                .count(dto.getCount())
                .buyDate(parsedBuyDate)
                .contents(dto.getContents())
                .fileList(fileList)
                .build();
    }
}