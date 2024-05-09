package com.SafeNet.Backend.api.Member.Service;

import com.SafeNet.Backend.global.util.RedisUtil;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailService{

    private final JavaMailSender emailSender;
    private final RedisUtil redisUtil;
    private static int number;

    public static void createKey(){
        number = (int)(Math.random() * (90000)) + 100000;// (int) Math.random() * (최댓값-최소값+1) + 최소값
    }

    public MimeMessage CreateMail(String mail){
        createKey();
        MimeMessage message = emailSender.createMimeMessage();
        try {
            message.setFrom();
            message.setRecipients(MimeMessage.RecipientType.TO, mail);
            message.setSubject("이메일 인증");
            String body = "";
            body += "<h3>" + "요청하신 인증 번호입니다." + "</h3>";
            body += "<h1>" + number + "</h1>";
            body += "<h3>" + "감사합니다." + "</h3>";
            message.setText(body,"UTF-8", "html");
        } catch (MessagingException e) {
            e.printStackTrace();
        }
        return message;
    }
    // 인증코드 이메일 발송
    public int sendMail(String mail)throws MessagingException{
        if (redisUtil.existData(mail)) {
            redisUtil.deleteData(mail);
        }
        MimeMessage message = CreateMail(mail);
        emailSender.send(message);

        return number;
    }
    //코드 검증
    public boolean verifyEmailCode(String mail, String verifyCode) {
        String codeFoundByEmail = redisUtil.getData(mail);
        log.info("code found by email: " + codeFoundByEmail);
        if (codeFoundByEmail == null) {
            return false;
        }
        return codeFoundByEmail.equals(verifyCode);
    }

}
