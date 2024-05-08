package com.SafeNet.Backend.global.config;

import com.SafeNet.Backend.domain.message.dto.MessageDto;
import com.SafeNet.Backend.domain.message.dto.MessageResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@RequiredArgsConstructor
@Configuration
public class RedisConfig {

    /**
     * redis에 발행(publish)된 메시지 처리를 위한 리스너 설정
     */
    @Bean
    public RedisMessageListenerContainer redisMessageListener(RedisConnectionFactory connectionFactory) {
        RedisMessageListenerContainer container = new RedisMessageListenerContainer();
        container.setConnectionFactory(connectionFactory);
        return container;
    }

    /**
     * Redis와 상호작용하기 위해 어플리케이션에서 사용할 redisTemplate설정
     */
    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, Object> redisTemplate = new RedisTemplate<>();
        redisTemplate.setConnectionFactory(connectionFactory);
        redisTemplate.setKeySerializer(new StringRedisSerializer()); // Key Serializer
        redisTemplate.setValueSerializer(new Jackson2JsonRedisSerializer<>(String.class)); // Value Serializer
        return redisTemplate;
    }
    /**
     * Redis에 메시지 내역을 저장하기 위한 template설정
     */
    @Bean
    public RedisTemplate<String, MessageDto> redisTemplateMessage(RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, MessageDto> redisTemplateMessage = new RedisTemplate<>();
        redisTemplateMessage.setConnectionFactory(connectionFactory);
        redisTemplateMessage.setKeySerializer(new StringRedisSerializer()); // Key Serializer
        redisTemplateMessage.setValueSerializer(new Jackson2JsonRedisSerializer<>(MessageDto.class)); // Value Serializer
        return redisTemplateMessage;
    }
}
