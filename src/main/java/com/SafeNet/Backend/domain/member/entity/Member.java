package com.SafeNet.Backend.domain.member.entity;

import com.SafeNet.Backend.global.util.BaseTimeEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;

@Getter
@NoArgsConstructor
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

    @Column(name = "name")
    @NotNull
    private String name;

    @Column(name = "phoneNumber")
    private String phoneNumber;

    @Column(name = "pwd")
    @NotNull
    private String pwd;

//    @Column(name = "region_id")
//    @NotNull
//    private int regionId;

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
