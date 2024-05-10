package com.SafeNet.Backend.api.Member.Controller;

import com.SafeNet.Backend.api.Member.Dto.EmailDto;
import com.SafeNet.Backend.api.Member.Entity.Member;
import com.SafeNet.Backend.api.Member.Service.EmailService;
import com.SafeNet.Backend.api.Member.Service.MemberService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.mail.MessagingException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.coyote.Response;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name= "Member Controller", description = "회원 API")
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/member")
public class MemberController {

    private final MemberService memberService;
    private final EmailService mailService;

    @Operation(summary = "회원가입", description = "회원가입을 승인합니다.")
    @PostMapping("/signup")
    public Response signUpUser(@RequestBody Member member){
        Response response = new Response();

        try{
            memberService.signUpUser(member);
            response.setMessage("회원가입을 성공적으로 완료했습니다.");
        }
        catch(Exception e){
            response.setMessage("회원가입을 하는 도중 오류가 발생했습니다."+ e.toString());
        }

        return response;
    }


    @Operation(summary = "이메일 인증", description = "이메일 인증번호를 입력받은 메일로 전송합니다.")
    @PostMapping("/sendCode")
    public String mailSend(EmailDto emailDto) throws MessagingException {
        log.info("EmailController.mailSend()");
        int number = mailService.sendMail(emailDto.getMail());

        String num = "" + number;
        return num;
    }
}
