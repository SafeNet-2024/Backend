package com.SafeNet.Backend.global.config;

import com.SafeNet.Backend.domain.message.dto.MessageDto;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.data.redis.repository.configuration.EnableRedisRepositories;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;


@Configuration
@EnableRedisRepositories
public class RedisConfig {

    @Value("${spring.data.redis.port}")
    private int port;

    @Value("${spring.data.redis.host}")
    private String host;

    @Bean
    public RedisConnectionFactory redisConnectionFactory() {
        return new LettuceConnectionFactory(host, port);
    }

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
    @Bean(name = "customRedisTemplate")
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
    @Bean(name = "redisTemplateMessage")
    public RedisTemplate<String, MessageDto> redisTemplateMessage(RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, MessageDto> redisTemplateMessage = new RedisTemplate<>();
        redisTemplateMessage.setConnectionFactory(connectionFactory);
        redisTemplateMessage.setKeySerializer(new StringRedisSerializer()); // Key Serializer
        redisTemplateMessage.setValueSerializer(new Jackson2JsonRedisSerializer<>(MessageDto.class)); // Value Serializer
        return redisTemplateMessage;
    }

    /**
     * RefreshToken 저장을 위한 redisTemplate 설정
     */
    @Bean(name = "tokenRedisTemplate")
    public RedisTemplate<String, String> tokenRedisTemplate(RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, String> tokenRedisTemplate = new RedisTemplate<>();
        tokenRedisTemplate.setConnectionFactory(connectionFactory);
        tokenRedisTemplate.setKeySerializer(new StringRedisSerializer());
        tokenRedisTemplate.setValueSerializer(new StringRedisSerializer());
        return tokenRedisTemplate;
    }
}

