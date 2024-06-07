package com.SafeNet.Backend.domain.member.service;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
public class JwtBlacklistService {
    private RedisTemplate<String, String> redisTemplate;

    public JwtBlacklistService(@Qualifier("tokenRedisTemplate") RedisTemplate<String, String> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public void addToBlacklist(String token, long expirationTime) {
        redisTemplate.opsForValue().set(token, "logout", expirationTime, TimeUnit.MILLISECONDS);
    }

    public boolean isBlacklisted(String token) {
        return redisTemplate.hasKey(token);
    }
}
