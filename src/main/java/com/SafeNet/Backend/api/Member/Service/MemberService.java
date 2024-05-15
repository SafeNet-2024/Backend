package com.SafeNet.Backend.api.Member.Service;

import com.SafeNet.Backend.api.Member.Dto.LoginRequestDto;
import com.SafeNet.Backend.api.Member.Dto.SignupRequestDto;
import com.SafeNet.Backend.api.Member.Dto.TokenResponseDto;
import com.SafeNet.Backend.api.Member.Entity.Member;
import com.SafeNet.Backend.api.Member.Repository.MemberRepository;
import com.SafeNet.Backend.api.Member.Repository.RefreshTokenRepository;
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
    public Member signUpUser(SignupRequestDto signupRequestDto) {
        Optional<Member> valiMember = memberRepository.findByEmail(signupRequestDto.getEmail());
        // 중복가입 방지
        if(valiMember.isPresent()){
            throw new CustomException("This email is already Exist "+ signupRequestDto.getEmail());
        }
        // 닉네임 중복검사
        else if (memberRepository.existsMemberByName(signupRequestDto.getName())) {
            throw new CustomException("This nickName is already Exist "+ signupRequestDto.getName());
        }
        Member member = Member.builder()
                .email(signupRequestDto.getEmail())
                .name(signupRequestDto.getName())
                .phoneNumber(signupRequestDto.getPhoneNumber())
                .pwd(passwordEncoder.encode(signupRequestDto.getPassword())) //비밀번호 암호화
                //.regionId()
                .build();

        memberRepository.save(member);

        return member;
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
            UsernamePasswordAuthenticationToken authenticationToken =
                    new UsernamePasswordAuthenticationToken(member.getEmail(), member.getPassword());
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
        }
    }
}
