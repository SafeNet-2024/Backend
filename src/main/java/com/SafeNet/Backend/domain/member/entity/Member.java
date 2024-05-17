package com.SafeNet.Backend.domain.member.entity;

import com.SafeNet.Backend.global.util.BaseTimeEntity;
import com.SafeNet.Backend.domain.file.entity.File;
import com.SafeNet.Backend.domain.postLike.entity.PostLike;
import com.SafeNet.Backend.domain.messageroom.entity.MessageRoom;
import com.SafeNet.Backend.domain.post.entity.Post;
import com.SafeNet.Backend.domain.region.entity.Region;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import lombok.*;

import java.util.Collection;
import java.util.List;

import static jakarta.persistence.FetchType.LAZY;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "member")
public class Member extends BaseTimeEntity implements UserDetails {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "member_id")
    private Long id;
    //코드상에 나오는 Username들은 모두 email을 뜻함
    @Column(name = "email", unique = true, nullable = false)
    private String email;

    @NotNull
    @Column(name = "name", nullable = false, unique = true, length = 100)
    private String name;

    @Column(name = "phoneNumber")
    private String phoneNumber;

    @Column(name = "pwd",nullable = false)
    @NotNull
    private String pwd;

    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "region_id", nullable = false)
    private Region region;
//    @Column(name = "region_id")
//    @NotNull
//    private int regionId;

    @OneToMany(mappedBy = "member", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    private List<MessageRoom> messageRooms;

    @OneToMany(mappedBy = "member", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    private List<Post> postList;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "member_id")
    @JsonIgnore
    private List<File> fileList; // 단방향 참조 (File 클래스에서 Member에 대한 참조 안함)

    @OneToMany(mappedBy = "member", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    private List<PostLike> postLikeList;


    @Builder
    public Member( String email, String name, String phoneNumber, String pwd ) {
        this.email=email;
        this.name= name;
        this.phoneNumber= phoneNumber;
        this.pwd= pwd;
        //this.regionId =regionId;

    }

    public Long getId() {
        return id;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return null;
    }

    @Override
    public String getPassword() {
        return this.pwd;
    }
    @Override
    public String getUsername() {
        return this.email;
    }
    // 계정 만료되었는지 (true - 만료 안됨)
    @Override
    public boolean isAccountNonExpired() {
        return true;
    }
    // 계정 잠겨있는지 (true - 안잠김)
    @Override
    public boolean isAccountNonLocked() {
        return true;
    }
    // 계정 비밀번호 만료되었는지 (true - 만료 X)
    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }
    // 계정 활성화 상태인지 (true - 활성화)
    @Override
    public boolean isEnabled() {
        return true;
    }

}
