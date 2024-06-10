package com.SafeNet.Backend.domain.post.service;

import com.SafeNet.Backend.domain.member.entity.Member;
import com.SafeNet.Backend.domain.member.repository.MemberRepository;
import com.SafeNet.Backend.domain.member.service.MemberService;
import com.SafeNet.Backend.domain.post.dto.PostResponseDto;
import com.SafeNet.Backend.domain.post.exception.PostException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class MemberItemService {
    private final CommonPostService commonPostService;
    private final MemberRepository memberRepository;

    public List<PostResponseDto> getPostsByMemberId(String email) {
        Member member = memberRepository.findByEmail(email).orElseThrow(() -> new PostException("Member not found with email: " + email, HttpStatus.NOT_FOUND)); // 유효한 멤버인지 검사
        return commonPostService.getPostsByMemberId(email);
    }

    public List<PostResponseDto> getLikedPostsByMemberId(String email) {
        Member member = memberRepository.findByEmail(email).orElseThrow(() -> new PostException("Member not found with email: " + email, HttpStatus.NOT_FOUND)); // 유효한 멤버인지 검사
        return commonPostService.getLikedPostsByMemberId(email);
    }
}
