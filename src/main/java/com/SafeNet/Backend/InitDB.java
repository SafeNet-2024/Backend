package com.SafeNet.Backend;

import com.SafeNet.Backend.domain.member.domain.Member;
import com.SafeNet.Backend.domain.member.repository.MemberRepository;
import com.SafeNet.Backend.domain.region.domain.Region;
import com.SafeNet.Backend.domain.region.repository.RegionRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Component
@RequiredArgsConstructor
public class InitDB {

    private final InitService initService;

    @PostConstruct
    public void init() {
        initService.userDBInit();
    }

    @Component
    @Transactional
    @RequiredArgsConstructor
    static class InitService {

        private final MemberRepository memberRepository;
        private final RegionRepository regionRepository;

        public void userDBInit() {
//            List<Member> memberList = memberRepository.findAll();
//            if (memberList.isEmpty()) {
            Region region = Region.builder()
                    .city("서울시")
                    .build();
            Member member = Member.builder()
                    .name("회원A")
                    .name("010-1111-2222")
                    .email("abc@gmail.com")
                    .pwd("1111")
                    .region(region)
                    .build();
            regionRepository.save(region);
            memberRepository.save(member);
//            }
        }
    }
}
