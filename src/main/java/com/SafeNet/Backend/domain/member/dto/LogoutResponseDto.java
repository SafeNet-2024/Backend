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
@Schema(description ="로그아웃 Dto")
public class LogoutResponseDto {
    @Schema(description = "로그아웃 결과", required = true, example = "로그아웃에 성공했습니다.")
    @NotNull
    String result;
}