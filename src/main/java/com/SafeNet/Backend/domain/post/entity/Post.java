package com.SafeNet.Backend.domain.post.entity;

import com.SafeNet.Backend.domain.post.dto.PostRequestDto;
import com.SafeNet.Backend.domain.postLike.entity.PostLike;
import com.SafeNet.Backend.domain.member.entity.Member;
import com.SafeNet.Backend.domain.messageroom.entity.MessageRoom;
import com.SafeNet.Backend.domain.file.entity.File;
import com.SafeNet.Backend.domain.region.entity.Region;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
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
    @NotBlank(message = "Title must not be empty")
    @Size(max = 100, message = "Title must be less than 100 characters")
    private String title;

    @Column(nullable = false)
    @Positive(message = "Cost must be a positive number")
    private int cost;

    @Column(nullable = false)
    @Positive(message = "Count must be a positive number")
    private int count;

    @Column(nullable = false)
    @NotNull(message = "Buy Date must not be null")
    private LocalDate buyDate;

    @Lob
    @Column(nullable = false)
    @NotBlank(message = "Contents must not be empty")
    private String contents;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime created;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updated;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    @Column(nullable = false)
    private PostStatus postStatus = PostStatus.거래가능; // 기본값: 거래가능

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @NotNull(message = "Category must not be null")
    private Category category;

    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "region_id")
    //@NotNull
    private Region region;

    @OneToMany(fetch = LAZY, mappedBy = "post", cascade = CascadeType.ALL)
    private List<MessageRoom> messageRoom;

    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    private List<PostLike> postLikeList;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "post_id")
    @Size(min = 2, max = 2, message = "File list must contain exactly 2 files")
    @JsonIgnore
    private List<File> fileList; // 단방향

    // 업데이트 메서드
    public void updatePost(PostRequestDto dto, LocalDate parsedBuyDate, List<File> fileList) {
        this.toBuilder()
                .title(dto.getTitle())
                .category(dto.getCategory())
                .cost(dto.getCost())
                .count(dto.getCount())
                .buyDate(parsedBuyDate)
                .contents(dto.getContents())
                .fileList(fileList)
                .build();
    }

    public void setPostStatus(PostStatus postStatus) {
        this.postStatus = postStatus;
    }
}