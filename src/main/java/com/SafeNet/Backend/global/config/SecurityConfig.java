package com.SafeNet.Backend.global.config;

import com.SafeNet.Backend.api.Member.Entity.UserDetailsImpl;
import com.SafeNet.Backend.api.Member.Service.EmailService;
import com.SafeNet.Backend.global.auth.JwtFilter;
import com.SafeNet.Backend.global.auth.JwtTokenProvider;
import com.SafeNet.Backend.global.exception.JwtAccessDeniedHandler;
import com.SafeNet.Backend.global.exception.JwtAuthenticationEntryPoint;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.cache.CacheProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;


@Configuration
@EnableWebSecurity
@AllArgsConstructor
public class SecurityConfig {

    private final JwtTokenProvider jwtTokenProvider;
    private final RedisTemplate redisTemplate;
    private final JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;
    private final JwtAccessDeniedHandler jwtAccessDeniedHandler;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())  // CSRF 보호 기능을 비활성화
                .exceptionHandling(exception -> exception
                        .authenticationEntryPoint(new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED)))  // 인증 실패시 HTTP 401 반환
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/", "/api/auth/**","/swagger-ui/**","/v3/api-docs/**").permitAll()  // 특정 경로에 대한 접근 허용
                        .anyRequest().authenticated())  // 나머지 요청은 인증 필요
                //.formLogin(form -> form
                //       .loginPage("/login").permitAll())  // 로그인 페이지 설정
                //.logout(logout -> logout
                //        .logoutSuccessUrl("/").permitAll())  // 로그아웃 성공시 리다이렉션 설정
                .exceptionHandling(authenticationManager -> authenticationManager
                    .accessDeniedHandler(jwtAccessDeniedHandler)
                    .authenticationEntryPoint(jwtAuthenticationEntryPoint))
                .addFilterBefore(new JwtFilter(jwtTokenProvider,redisTemplate), UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    AuthenticationManager authenticationManager(
            AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }
}
