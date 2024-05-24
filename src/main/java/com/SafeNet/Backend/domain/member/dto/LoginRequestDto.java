package com.SafeNet.Backend.domain.member.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;


@Data
@Schema(description ="로그인 요청 Dto")
public class LoginRequestDto {
    private String email;
    private String password;

    public UsernamePasswordAuthenticationToken toAuthenticationToken() {
        return new UsernamePasswordAuthenticationToken(email, password);
    }



}
