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
@Schema(description ="회원가입 반환 Dto")
public class SignupResponseDto {
    @Schema(description = "회원가입 결과", required = true, example = "회원가입에 성공했습니다.")
    @NotNull
    String result;
}
