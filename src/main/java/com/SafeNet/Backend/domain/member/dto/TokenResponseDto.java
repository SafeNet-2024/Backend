package com.SafeNet.Backend.domain.member.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.ToString;

@Builder
@Data
@AllArgsConstructor
@ToString
@Schema(description ="로그인 반환 Dto")
public class TokenResponseDto {
    @Schema(description = "토큰 타입", required = true, example = "Bearer")
    @NotNull
    private  String grantType;

    @Schema(description = "Access Token", required = true, example = "eyJhbGciOiJIUzI1NiIsInR5cCI...")
    @NotNull
    private String accessToken;

    @Schema(description = "Refresh Token", required = true, example = "eyJhbGciOiJIUzI1NiIsInR5cCI...")
    @NotNull
    private String refreshToken;


}
