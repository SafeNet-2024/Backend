package com.SafeNet.Backend.global.auth;

import com.SafeNet.Backend.global.exception.CustomException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.data.redis.RedisConnectionFailureException;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/*
 * 헤더(Authorization)에 있는 토큰을 꺼내 이상이 없는 경우 SecurityContext에 저장
 * Request 이전에 작동
 */
@Slf4j
public class JwtFilter extends OncePerRequestFilter {

    private final JwtTokenProvider jwtTokenProvider;
    private final RedisTemplate<String, String> redisTemplate;

    public JwtFilter(JwtTokenProvider jwtTokenProvider, @Qualifier("tokenRedisTemplate") RedisTemplate<String, String> redisTemplate) {
        this.jwtTokenProvider = jwtTokenProvider;
        this.redisTemplate = redisTemplate;
    }
    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        // 1. Request Header 에서 JWT 토큰 추출
        String token = jwtTokenProvider.resolveToken(request);
        try {
            // 2. validateToken 으로 토큰 유효성 검사
            if (token != null && jwtTokenProvider.validateToken(token)) {
                log.info("dofilterInternal : [토큰유효성 검사 통과.]");
                //Redis 에 해당 accessToken logout 여부 확인 : Logout이면 Redis에 특정값이 저장되어있음
                String isLogout = (String)redisTemplate.opsForValue().get(token);
                if (ObjectUtils.isEmpty(isLogout)) { // 로그아웃 상태가 아니라면
                    // 토큰이 유효할 경우 토큰에서 Authentication 객체를 가지고 와서 SecurityContext 에 저장
                    Authentication authentication = jwtTokenProvider.getAuthentication(token);
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                }else {
                    log.info("유효한 JWT 토큰이 없습니다. (로그아웃된 토큰) ");
                    throw new AuthenticationException("로그아웃된 토큰입니다.") {};
                }
            }
        } catch (RedisConnectionFailureException e) {
            SecurityContextHolder.clearContext();
            writeErrorResponse(response, HttpStatus.INTERNAL_SERVER_ERROR, "REDIS_ERROR", e);
        } catch (AuthenticationException e) {
            SecurityContextHolder.clearContext();
            writeErrorResponse(response, HttpStatus.UNAUTHORIZED, "로그아웃된 계정입니다.", e);
        }
        try {
            filterChain.doFilter(request, response);
        } catch (Exception e) {
            writeErrorResponse(response, HttpStatus.BAD_REQUEST, "Error during filtering request", e);
        }
    }

    private void writeErrorResponse(HttpServletResponse response, HttpStatus status, String error, Exception e) throws IOException {
        response.setStatus(status.value());
        response.setContentType("application/json");
        response.getWriter().write("{\"error\": \"" + error + "\", \"message\": \"" + e.getMessage() + "\"}");
        response.getWriter().flush();
    }
}
