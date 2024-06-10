package com.SafeNet.Backend.domain.post.service;

import com.SafeNet.Backend.domain.member.entity.Member;
import com.SafeNet.Backend.domain.member.repository.MemberRepository;
import com.SafeNet.Backend.domain.post.dto.PostResponseDto;
import com.SafeNet.Backend.domain.post.entity.Category;
import com.SafeNet.Backend.domain.post.entity.Post;
import com.SafeNet.Backend.domain.post.exception.PostException;
import com.SafeNet.Backend.domain.post.repository.PostRepository;
import com.SafeNet.Backend.domain.postLike.entity.PostLike;
import com.SafeNet.Backend.domain.postLike.repository.PostLikeRepository;
import com.SafeNet.Backend.domain.post.util.PostDtoConverter;
import com.SafeNet.Backend.domain.region.entity.Region;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CommonPostService {
    private final PostRepository postRepository;
    private final MemberRepository memberRepository;
    private final PostLikeRepository postLikeRepository;

    public Member getMemberByEmail(String email) {
        return memberRepository.findByEmail(email).orElseThrow(() -> new PostException("Member not found with email: " + email, HttpStatus.NOT_FOUND));
    }
    private Long getMemberRegionId(String email) {
        Member member = getMemberByEmail(email);
        Region region = member.getRegion();
        return region.getId();
    }

    public List<PostResponseDto> convertPostsToDto(List<Post> posts, Long memberId, String email) {
        return posts.stream().map(post -> {
            boolean isLikedByCurrentUser = postLikeRepository.existsByPostIdAndMemberId(post.getId(), memberId);
            return PostDtoConverter.convertToDto(post, isLikedByCurrentUser, post.getMember().getEmail().equals(email),post.getPostStatus());
        }).collect(Collectors.toList());
    }

    // 게시물 리스트 조회
    public List<PostResponseDto> getAllPosts(String email) {
        Member member = getMemberByEmail(email);
        Long memberId = member.getId(); // MemberId 추출
        Region region = member.getRegion(); // 지역 추출
        Long memberRegionId = region.getId(); // 멤버의 RegionId 추출
        List<Post> posts = postRepository.findByRegion_Id(memberRegionId);
        return convertPostsToDto(posts, memberId, email);
    }

    // 사용자가 등록한 게시물 조회
    public List<PostResponseDto> getPostsByMemberId(String email) {
        Member member = getMemberByEmail(email);
        Long memberId = member.getId();
        List<Post> posts = postRepository.findByMember_Id(memberId);
        return convertPostsToDto(posts, memberId, email);
    }

    // 사용자가 좋아요 누른 게시물 조회
    public List<PostResponseDto> getLikedPostsByMemberId(String email) {
        Member member = getMemberByEmail(email);
        Long memberId = member.getId();
        List<Post> posts = postLikeRepository.findByMember_Id(memberId).stream()
                .map(PostLike::getPost)
                .collect(Collectors.toList());
        return convertPostsToDto(posts, memberId, email);
    }

    // 키워드로 게시물 조회
    public List<PostResponseDto> searchPostsByKeyword(String keyword, String email) {
        int MAX_LENGTH = 50;
        if (keyword.length() > MAX_LENGTH) { // 입력한 keyword가 50글자보다 길다면
            throw new PostException("검색어가 너무 깁니다. " + MAX_LENGTH + " 글자 이하로 입력해주세요.", HttpStatus.BAD_REQUEST);
        }
        Long memberRegionId = getMemberRegionId(email);
        // 특정 지역의 정보이면서 제목을 포함한 경우 & 특정 지역의 정보이면서 내용을 포함한 경우
        List<Post> posts = postRepository.findByRegionIdAndTitleOrContentsContaining(memberRegionId, keyword);
        return getPostResponseDtos(email, posts);
    }

    // 카테고리로 게시물 조회
    public List<PostResponseDto> searchPostsByCategory(Category category, String email) {
        Long memberRegionId = getMemberRegionId(email);
        List<Post> posts = postRepository.findByRegion_IdAndCategory(memberRegionId, category);
        return getPostResponseDtos(email, posts);
    }

    // 게시물 생성 일자 역순으로 게시물 조회
    public List<PostResponseDto> sortPostsByCreated(String email) {
        Long memberRegionId = getMemberRegionId(email);
        List<Post> posts = postRepository.findByRegion_IdOrderByCreatedDesc(memberRegionId);
        return getPostResponseDtos(email, posts);
    }

    // 구매일자 역순으로 게시물 조회
    public List<PostResponseDto> sortPostsByBuyDate(String email) {
        Long memberRegionId = getMemberRegionId(email);
        List<Post> posts = postRepository.findByRegion_IdOrderByBuyDateDesc(memberRegionId);
        return getPostResponseDtos(email, posts);
    }

    private List<PostResponseDto> getPostResponseDtos(String email, List<Post> posts) {
        Member member = getMemberByEmail(email);
        Long memberId = member.getId();
        return convertPostsToDto(posts, memberId, email);
    }
}

