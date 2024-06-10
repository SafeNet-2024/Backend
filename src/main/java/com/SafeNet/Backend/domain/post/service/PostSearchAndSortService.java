package com.SafeNet.Backend.domain.post.service;

import com.SafeNet.Backend.domain.member.entity.Member;
import com.SafeNet.Backend.domain.member.repository.MemberRepository;
import com.SafeNet.Backend.domain.post.dto.PostResponseDto;
import com.SafeNet.Backend.domain.post.entity.Category;
import com.SafeNet.Backend.domain.post.exception.PostException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class PostSearchAndSortService {
    private final CommonPostService commonPostService;
    private final MemberRepository memberRepository;
    public List<PostResponseDto> searchByKeyWord(String keyword, String email) {
        Member member = memberRepository.findByEmail(email).orElseThrow(() -> new PostException("Member not found with email: " + email, HttpStatus.NOT_FOUND)); // 유효한 멤버인지 검사
        return commonPostService.searchPostsByKeyword(keyword, email);
    }

    public List<PostResponseDto> searchByCategory(Category category, String email) {
        Member member = memberRepository.findByEmail(email).orElseThrow(() -> new PostException("Member not found with email: " + email, HttpStatus.NOT_FOUND)); // 유효한 멤버인지 검사
        return commonPostService.searchPostsByCategory(category, email);
    }

    public List<PostResponseDto> sortByCreated(String email) {
        Member member = memberRepository.findByEmail(email).orElseThrow(() -> new PostException("Member not found with email: " + email, HttpStatus.NOT_FOUND)); // 유효한 멤버인지 검사
        return commonPostService.sortPostsByCreated(email);
    }

    public List<PostResponseDto> sortByBuyDate(String email) {
        Member member = memberRepository.findByEmail(email).orElseThrow(() -> new PostException("Member not found with email: " + email, HttpStatus.NOT_FOUND)); // 유효한 멤버인지 검사
        return commonPostService.sortPostsByBuyDate(email);
    }
}
