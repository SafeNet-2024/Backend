package com.SafeNet.Backend.domain.member.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.data.annotation.Id;

@Getter
@AllArgsConstructor
//@RedisHash(value = "refreshToken", timeToLive = 60 * 60 * 24 * 7)
public class RefreshToken {

    @Id
    private String refreshToken;
    private Long memberId;


    public RefreshToken(Long memberId, String refreshToken) {
        this.memberId = memberId;
        this.refreshToken = refreshToken;
    }

    public RefreshToken update(String newRefreshToken) {
        this.refreshToken = newRefreshToken;
        return this;
    }
}