package com.SafeNet.Backend.domain.member.service;

import com.SafeNet.Backend.domain.member.dto.*;
import com.SafeNet.Backend.domain.member.entity.Member;
import com.SafeNet.Backend.domain.member.repository.MemberRepository;
import com.SafeNet.Backend.domain.region.RegionParser;
import com.SafeNet.Backend.domain.region.entity.Region;
import com.SafeNet.Backend.domain.region.repository.RegionRepository;
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
    private  final RegionRepository regionRepository;
    /*
     ** 회원가입
     */
    @Transactional
    public TokenResponseDto signUpUser(SignupRequestDto signupRequestDto) {
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
                .build();

        memberRepository.save(member);

        try {
            UsernamePasswordAuthenticationToken authenticationToken =
                    new UsernamePasswordAuthenticationToken(signupRequestDto.getEmail(), signupRequestDto.getPassword()); //암호화된 member객체 pwd대신 dto의 패스워드를 넣어 사용
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
        }catch (Exception e) {
            log.error("회원가입 시도 중 예외 발생: {}", e.getMessage(), e);
            throw new CustomException("토큰 발급중 알 수 없는 문제가 발생했습니다:  " + e.getMessage());
        }
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
    /*
     ** 로그아웃
     */
    public void logout(String email, String atk) {
        //Token에서 로그인한 사용자 정보 get해 로그아웃 처리
        try {
            jwtTokenProvider.logout(email, atk); //Token 삭제
        }catch (CustomException ex) {
            throw new CustomException("이미 로그아웃된 유저입니다");
        }
    }
    /*
     ** 내정보 업데이트
     */
    @Transactional
    public void updateMember(String email, MemberUpdateDto memberUpdateDto) {
        Member existingMember = memberRepository.findByEmail(email).orElseThrow();

        Region parsedRegion = RegionParser.parseRegion(memberUpdateDto.getAddress());
        Region region = regionRepository.findByCityAndCountyAndDistrict(
                parsedRegion.getCity(),
                parsedRegion.getCounty(),
                parsedRegion.getDistrict()
        ).orElseGet(() -> regionRepository.save(parsedRegion));
        existingMember.updateProfile(memberUpdateDto.getName(),
                memberUpdateDto.getPhoneNumber(),
                passwordEncoder.encode(memberUpdateDto.getPassword()),
                region);
        memberRepository.save(existingMember);
    }
    /*
     ** 주소 업데이트
     */
    @Transactional
    public void updateMemberAddress(String email, AddressRequestDto addressRequestDto) {
        Member member = memberRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("No member found with email: " + email));


        Region parsedRegion = RegionParser.parseRegion(addressRequestDto.getAddress());

        Region region = regionRepository.findByCityAndCountyAndDistrict(
                parsedRegion.getCity(),
                parsedRegion.getCounty(),
                parsedRegion.getDistrict()
        ).orElseGet(() -> regionRepository.save(parsedRegion));

        member.updateProfile(member.getName(), member.getPhoneNumber(), member.getPwd(), region);
        memberRepository.save(member);
    }
}
