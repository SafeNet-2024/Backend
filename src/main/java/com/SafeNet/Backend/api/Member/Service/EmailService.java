package com.SafeNet.Backend.api.Member.Service;


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
    public static int createKey(){
        return (int)(Math.random() * (90000)) + 100000;// (int) Math.random() * (최댓값-최소값+1) + 최소값
    }

    public MimeMessage CreateMail(String mail, int number){

        MimeMessage message = emailSender.createMimeMessage();
        try {
            message.setFrom();
            message.setRecipients(MimeMessage.RecipientType.TO, mail);
            message.setSubject("[GroShare] 이메일 인증번호를 확인해주세요.");
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
        int number = createKey();
        MimeMessage message = CreateMail(mail, number);
        emailSender.send(message);

        return number;
    }

}
