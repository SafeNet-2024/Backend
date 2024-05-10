package com.SafeNet.Backend.global.config;

import com.SafeNet.Backend.api.Member.Service.EmailService;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;

@RequiredArgsConstructor
@Configuration
@EnableWebSecurity
@AllArgsConstructor
public class SecurityConfig {

    @Autowired
    EmailService emailService;


    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())  // CSRF 보호 기능을 비활성화
                .exceptionHandling(exception -> exception
                        .authenticationEntryPoint(new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED)))  // 인증 실패시 HTTP 401 반환
                .authorizeRequests(auth -> auth
                        .requestMatchers("/", "/api/email/**", "/api/member/**").permitAll()  // 특정 경로에 대한 접근 허용
                        .anyRequest().authenticated())  // 나머지 요청은 인증 필요
                .formLogin(form -> form
                        .loginPage("/login").permitAll())  // 로그인 페이지 설정
                .logout(logout -> logout
                        .logoutSuccessUrl("/").permitAll());  // 로그아웃 성공시 리다이렉션 설정

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

}
