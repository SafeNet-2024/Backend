package com.SafeNet.Backend.api.Member.Dto;

import com.SafeNet.Backend.api.Member.Entity.Member;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import org.apache.catalina.User;

@Data // getter/setter, requiredArgsController, ToString 등 합쳐놓은 세트
@Builder
@AllArgsConstructor
public class SignupRequestDto {

    @NotEmpty(message = "[필수] 이메일을 입력해주세요")
    @Email
    private String email;

    @NotEmpty(message = "[필수] 비밀번호를 입력해주세요")
    @Pattern(regexp = " ^(?=.*[A-Za-z])(?=.*\\d)[A-Za-z\\d~!@#$%^&*()+|=]{8,16}$",
            message = "8자 이상이며 최대 16자까지 허용. 반드시 숫자, 문자 포함")
    private String password;

    @NotEmpty(message = "[필수] 닉네임을 입력해주세요")
    @Size(min=2, message = "닉네임은 최소 두 글자 이상입니다")
    private String name;

    private String phoneNumber;

    //private int regionId;

    @Builder
    public Member toEntity(){
        return Member.builder()
                .email(email)
                .name(name)
                .pwd(password)
                .phoneNumber(phoneNumber)
                //.regionId(regionId)
                .build();
    }
}
