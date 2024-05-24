package com.SafeNet.Backend.domain.member.service;

import com.SafeNet.Backend.domain.member.dto.LoginRequestDto;
import com.SafeNet.Backend.domain.member.dto.SignupRequestDto;
import com.SafeNet.Backend.domain.member.dto.TokenResponseDto;
import com.SafeNet.Backend.domain.member.entity.Member;
import com.SafeNet.Backend.domain.member.entity.UserDetailsImpl;
import com.SafeNet.Backend.domain.member.repository.MemberRepository;
import com.SafeNet.Backend.global.auth.JwtTokenProvider;
import com.SafeNet.Backend.global.exception.CustomException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class MemberService {

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final AuthenticationManager authenticationManager;
    /*
     ** 회원가입
     */
    @Transactional
    public void signUpUser(SignupRequestDto signupRequestDto) {
        Optional<Member> valiMember = memberRepository.findByEmail(signupRequestDto.getEmail());
        // 중복가입 방지
        if (valiMember.isPresent()) {
            throw new CustomException(" This email is already Exist ");
        }
        // 닉네임 중복검사
        else if (memberRepository.existsMemberByName(signupRequestDto.getName())) {
            throw new CustomException(" This nickName is already Exist ");
        }
        Member member = Member.builder()
                .email(signupRequestDto.getEmail())
                .name(signupRequestDto.getName())
                .phoneNumber(signupRequestDto.getPhoneNumber())
                .pwd(passwordEncoder.encode(signupRequestDto.getPassword())) //비밀번호 암호화
                //.regionId()
                .build();

        memberRepository.save(member);
    }
    /*
     ** 로그인
     */
    @Transactional
    public TokenResponseDto login(LoginRequestDto loginRequestDto) throws Exception {
        Optional<Member> existingMember = memberRepository.findByEmail(loginRequestDto.getEmail());
        // 존재하지 않는 회원에 대한 에러처리
        if (existingMember.isEmpty()) {
            throw new CustomException("존재하지 않는 이메일입니다.");
            //throw new BadCredentialsException("존재하지 않는 이메일입니다.");
        }
        else {
            Member member = existingMember.get();
            if (!passwordEncoder.matches(loginRequestDto.getPassword(), member.getPassword())) {
                throw new BadCredentialsException("비밀번호가 일치하지 않습니다.");
            }
            try {
                UsernamePasswordAuthenticationToken authenticationToken =
                        new UsernamePasswordAuthenticationToken(loginRequestDto.getEmail(), loginRequestDto.getPassword()); //암호화된 member객체 pwd대신 dto의 패스워드를 넣어 사용
                Authentication authentication = authenticationManager.authenticate(authenticationToken);
                SecurityContextHolder.getContext().setAuthentication(authentication);
                String accessToken = jwtTokenProvider.createAccessToken(authentication);
                String refreshToken = jwtTokenProvider.createRefreshToken(authentication);

                log.info("[로그인 알림] {} 회원님이 로그인했습니다.", member.getId());


                return TokenResponseDto.builder()
                        .grantType("Bearer")
                        .accessToken(accessToken)
                        .refreshToken(refreshToken)
                        .build();
            }catch (BadCredentialsException e){
                log.info("로그인 시도 - 인증 실패: {}", e.getMessage());
                throw new CustomException("자격 증명에 실패하였습니다. " + e.getMessage());
            }catch (Exception e) {
                log.error("로그인 시도 중 예외 발생: {}", e.getMessage(), e);
                throw new CustomException("로그인 중 알 수 없는 오류가 발생했습니다. " + e.getMessage());
            }
        }
    }

    public void logout(String email) {
        //Token에서 로그인한 사용자 정보 get해 로그아웃 처리
        try {
            jwtTokenProvider.logout("JWT_TOKEN:" + email); //Token 삭제
        }catch (CustomException ex) {
            throw new CustomException("이미 로그아웃된 유저입니다");
        }
    }
}
