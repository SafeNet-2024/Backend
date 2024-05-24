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
@Schema(description ="이메일 인증번호 발송 Dto")
public class AuthCodeResponseDto {
    @Schema(description = "인증번호", required = true, example = "pmuEg2G7")
    @NotNull
    String AuthenticationCode;
}