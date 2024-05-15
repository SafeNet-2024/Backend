package com.SafeNet.Backend.api.Member.Controller;

import com.SafeNet.Backend.api.Member.Dto.EmailDto;
import com.SafeNet.Backend.api.Member.Dto.LoginRequestDto;
import com.SafeNet.Backend.api.Member.Dto.SignupRequestDto;
import com.SafeNet.Backend.api.Member.Dto.TokenResponseDto;
import com.SafeNet.Backend.api.Member.Service.EmailService;
import com.SafeNet.Backend.api.Member.Service.MemberService;
import com.SafeNet.Backend.global.exception.CustomException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.mail.MessagingException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.coyote.Response;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name= "Member Controller", description = "회원 API")
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth")
public class MemberController {

    private final MemberService memberService;
    private final EmailService mailService;

    @Operation(summary = "회원가입", description = "회원가입을 승인합니다.")
    @PostMapping("/signup")
    public ResponseEntity<Response> signUpUser(@RequestBody SignupRequestDto signupRequestDto){
        Response response = new Response();
        HttpStatus status = HttpStatus.OK;

        try{
            memberService.signUpUser(signupRequestDto);
            response.setMessage("회원가입을 성공적으로 완료했습니다.");
            log.info("회원가입을 성공적으로 완료했습니다.");
        }
        catch(CustomException e){
            response.setMessage("회원가입을 하는 도중 오류가 발생했습니다."+ e.toString());
            status = HttpStatus.BAD_REQUEST; // 요청 오류로 인식
            log.info("회원가입을 하는 도중 오류가 발생했습니다."+ e.toString());
        }
        catch (Exception e) { // 기타 예외 발생 시
            response.setMessage("서버 내부 오류가 발생했습니다.");
            status = HttpStatus.INTERNAL_SERVER_ERROR; // 서버 내부 오류로 인식
        }

        return ResponseEntity.status(status).body(response);
    }


    @Operation(summary = "이메일 인증", description = "이메일 인증번호를 입력받은 메일로 전송합니다.")
    @PostMapping("/sendCode")
    public String mailSend(EmailDto emailDto) throws MessagingException {
        log.info("EmailController.mailSend()");
        int number = mailService.sendMail(emailDto.getMail());

        String num = "" + number;
        return num;
    }

    @Operation(summary = "로그인", description = "로그인을 승인합니다.")
    @PostMapping("/login")
    public ResponseEntity<TokenResponseDto> login(@RequestBody LoginRequestDto loginRequestDto){
        try{
            TokenResponseDto token = memberService.login(loginRequestDto);
            String message = "회원가입을 성공적으로 완료했습니다.";
            return ResponseEntity.ok().body(token);
        }
        catch(Exception e){
            throw new CustomException("회원가입을 하는 도중 오류가 발생했습니다."+ e.toString());
        }

    }



}
