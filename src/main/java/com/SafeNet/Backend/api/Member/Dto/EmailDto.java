package com.SafeNet.Backend.api.Member.Dto;

import lombok.Data;

@Data
public class EmailDto {
    // 이메일 주소
    private String mail;
    // 인증 코드
    private String verifyCode;
}
