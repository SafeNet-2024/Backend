package com.SafeNet.Backend.domain.member.controller;

import com.SafeNet.Backend.domain.member.dto.*;
import com.SafeNet.Backend.domain.member.entity.UserDetailsImpl;
import com.SafeNet.Backend.domain.member.service.EmailService;
import com.SafeNet.Backend.domain.member.service.MemberService;
import com.SafeNet.Backend.domain.message.dto.MessageDto;
import com.SafeNet.Backend.global.exception.CustomException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.mail.MessagingException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

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
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "회원가입에 성공했습니다", content = @Content(mediaType = "application/json", schema = @Schema(implementation = String.class))),
            @ApiResponse(responseCode = "400", description = "회원가입에 실패했습니다.", content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "500", description = "서버 오류", content = @Content(mediaType = "application/json"))
    })
    public ResponseEntity<SignupResponseDto> signUpUser(@RequestBody SignupRequestDto signupRequestDto){
        String message = "";
        HttpStatus status = HttpStatus.OK;

        try{
            memberService.signUpUser(signupRequestDto);
            message ="회원가입을 성공적으로 완료했습니다.";
            status = HttpStatus.OK;
        }
        catch(Exception e){
            message = "회원가입을 하는 도중 오류가 발생했습니다."+ e.getMessage();
            status = HttpStatus.BAD_REQUEST; // 요청 오류로 인식
        }
        log.info(status+ message);
        return ResponseEntity.status(status)
                .body(SignupResponseDto.builder()
                        .result(message)
                        .build());
    }


    @Operation(summary = "이메일 인증", description = "이메일 인증번호를 입력받은 메일로 전송합니다.")
    @PostMapping("/sendCode")
    public ResponseEntity<AuthCodeResponseDto> mailSend(EmailDto emailDto) throws MessagingException {
        //TODO: 이메일 중복 체크
        log.info("EmailController.mailSend()");
        int number = mailService.sendMail(emailDto.getEmail());

        String num = "" + number;
        return ResponseEntity.ok(AuthCodeResponseDto.builder().AuthenticationCode(num).build());
    }

    @Operation(summary = "로그인", description = "토큰을 발급하고 redis에 저장합니다.")
    @PostMapping("/login")
    public ResponseEntity<TokenResponseDto> login(@RequestBody LoginRequestDto loginRequestDto){
        TokenResponseDto token= null;
        try{
            token = memberService.login(loginRequestDto);
            String message = "로그인을 성공적으로 완료했습니다.";

        }
        catch(Exception e){
            log.info(e.getMessage());
            throw new CustomException("로그인 하는 도중 오류가 발생했습니다. "+ e.getMessage());
        }

        return ResponseEntity.ok().body(token);

    }

    @PostMapping(value = "/logout")
    @Operation(summary = "로그아웃", description = "JWt 토큰을 redis에서 삭제합니다")
    public ResponseEntity<Void> logout(            @RequestHeader(name = "ACCESS_TOKEN", required = false) String accessToken,
                                                   @RequestHeader(name = "REFRESH_TOKEN", required = false) String refreshToken) {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        UserDetailsImpl userDetails = (UserDetailsImpl) principal;
        String email = userDetails.getUsername();
        log.info("토큰으로부터 이메일을 추출하였습니다.: "+email);
        memberService.logout(email);
        return ResponseEntity.ok().build();
    }

    //TODO: 위치정보 저장 api 필요

}
