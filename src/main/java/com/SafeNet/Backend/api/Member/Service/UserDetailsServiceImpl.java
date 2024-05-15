package com.SafeNet.Backend.api.Member.Service;

import com.SafeNet.Backend.api.Member.Entity.Member;
import com.SafeNet.Backend.api.Member.Entity.UserDetailsImpl;
import com.SafeNet.Backend.api.Member.Repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
@Slf4j
@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {
    private  final MemberRepository memberRepository;


    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        try {
            Member member = memberRepository.findByEmail(email).orElseThrow();
            UserDetailsImpl userDetails = new UserDetailsImpl();
            userDetails.setMember(member);

            return userDetails;
        } catch (Exception e) {
            throw new RuntimeException("해당 사용자가 DB에 없습니다."+ e);
        }
    }
}
