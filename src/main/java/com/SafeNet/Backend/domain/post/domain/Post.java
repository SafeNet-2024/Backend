package com.SafeNet.Backend.domain.post.domain;

import com.SafeNet.Backend.domain.likes.domain.Likes;
import com.SafeNet.Backend.domain.member.domain.Member;
import com.SafeNet.Backend.domain.messageRoom.domain.MessageRoom;
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
    private List<File> fileList; // 단방향 참조
}
