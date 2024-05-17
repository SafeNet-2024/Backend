package com.SafeNet.Backend;

import com.SafeNet.Backend.domain.member.entity.Member;
import com.SafeNet.Backend.domain.member.repository.MemberRepository;
import com.SafeNet.Backend.domain.region.entity.Region;
import com.SafeNet.Backend.domain.region.repository.RegionRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

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
            String cityName = "용인시";
            String memberEmail = "song@gmail.com";

            Optional<Region> existingRegion = regionRepository.findByCity(cityName);
            Region region;
            if (existingRegion.isEmpty()) {
                region = Region.builder()
                        .city(cityName)
                        .build();
                regionRepository.save(region);
            } else {
                region = existingRegion.get();
            }

            Optional<Member> existingMember = memberRepository.findByEmail(memberEmail);
            if (existingMember.isEmpty()) {
                Member member = Member.builder()
                        .name("눈송이")
                        .phoneNumber("010-3333-4444")
                        .email(memberEmail)
                        .pwd("3333")
                        .region(region)
                        .build();
                memberRepository.save(member);
            }
        }
    }
}
