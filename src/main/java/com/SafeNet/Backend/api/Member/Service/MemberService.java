package com.SafeNet.Backend.api.Member.Service;

import com.SafeNet.Backend.api.Member.Entity.Member;
import com.SafeNet.Backend.api.Member.Repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class MemberService {

    private final MemberRepository memberRepository;

    public void signUpUser(Member member) {
    }
}
