package com.SafeNet.Backend.global.config;

import com.SafeNet.Backend.global.auth.JwtTokenProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Component;

import java.util.Collections;

// WebSocket 메시지의 헤더에서 ACCESS_TOKEN을 추출하고 검증
// 유효한 토큰이 있는 경우 사용자 인증 정보를 설정하고, 유효하지 않은 경우 연결을 차단
@Component
public class AuthChannelInterceptor implements ChannelInterceptor {

    private final JwtTokenProvider jwtTokenProvider;

    @Autowired
    public AuthChannelInterceptor(JwtTokenProvider jwtTokenProvider) {
        this.jwtTokenProvider = jwtTokenProvider;
    }

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(message);
        if (StompCommand.CONNECT.equals(accessor.getCommand())) { // CONNECT 프레임은 서버에 대한 인증 및 기타 설정과 관련된 정보를 전송하기 위해 사용
            String token = accessor.getFirstNativeHeader("ACCESS_TOKEN");
            if (token != null && token.startsWith("Bearer ")) {
                token = token.substring(7);
                if (jwtTokenProvider.validateToken(token)) {
                    String username = jwtTokenProvider.getAuthentication(token).getName();
                    accessor.setUser(new UsernamePasswordAuthenticationToken(username, null, Collections.emptyList()));
                } else {
                    throw new IllegalArgumentException("Invalid or expired token");
                }
            } else {
                throw new IllegalArgumentException("Missing or invalid ACCESS_TOKEN header");
            }
        }
        return message;
    }
}