package com.SafeNet.Backend.api.Member.Controller;

import com.SafeNet.Backend.api.Member.Dto.EmailDto;
import com.SafeNet.Backend.api.Member.Service.EmailService;
import jakarta.mail.MessagingException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/email")
public class EmailController {

    private final EmailService mailService;

    // 인증코드 메일 발송
    @PostMapping("/send")
    public String mailSend(EmailDto emailDto) throws MessagingException {
        log.info("EmailController.mailSend()");
        mailService.sendMail(emailDto.getMail());
        return "인증코드가 발송되었습니다.";
    }

    // 인증코드 인증
    @PostMapping("/verify")
    public String verify(EmailDto emailDto) {
        log.info("EmailController.verify()");
        boolean isVerify = mailService.verifyEmailCode(emailDto.getMail(), emailDto.getVerifyCode());
        return isVerify ? "인증이 완료되었습니다." : "인증 실패하셨습니다.";
    }
}
