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

    public List<PostResponseDto> convertPostsToDto(List<Post> posts, Long memberId) {
        return posts.stream().map(post -> {
            boolean isLikedByCurrentUser = postLikeRepository.existsByPostIdAndMemberId(post.getId(), memberId);
            return PostDtoConverter.convertToDto(post, isLikedByCurrentUser);
        }).collect(Collectors.toList());
    }

    // 게시물 리스트 조회
    public List<PostResponseDto> getAllPosts(String email) {
        Member member = getMemberByEmail(email);
        Long memberId = member.getId(); // MemberId 추출
        Region region = member.getRegion();
        Long memberRegionId = region.getId(); // 멤버의 RegionId 추출
        List<Post> posts = postRepository.findByRegion_Id(memberRegionId);
        return convertPostsToDto(posts, memberId);
    }

    // 사용자가 등록한 게시물 조회
    public List<PostResponseDto> getPostsByMemberId(String email) {
        Member member = getMemberByEmail(email);
        Long memberId = member.getId();
        List<Post> posts = postRepository.findByMember_Id(memberId);
        return convertPostsToDto(posts, memberId);
    }

    // 사용자가 좋아요 누른 게시물 조회
    public List<PostResponseDto> getLikedPostsByMemberId(String email) {
        Member member = getMemberByEmail(email);
        Long memberId = member.getId();
        List<Post> posts = postLikeRepository.findByMember_Id(memberId).stream()
                .map(PostLike::getPost)
                .collect(Collectors.toList());
        return convertPostsToDto(posts, memberId);
    }

    // 키워드로 게시물 조회
    public List<PostResponseDto> searchPostsByKeyword(String keyword, String email) {
        List<Post> posts = postRepository.findByTitleContainingOrContentsContaining(keyword, keyword);
        return getPostResponseDtos(email, posts);
    }

    // 카테고리로 게시물 조회
    public List<PostResponseDto> searchPostsByCategory(Category category, String email) {
        List<Post> posts = postRepository.findByCategory(category);
        return getPostResponseDtos(email, posts);
    }

    // 게시물 생성 일자 역순으로 게시물 조회
    public List<PostResponseDto> sortPostsByCreated(String email) {
        List<Post> posts = postRepository.findAllByOrderByCreatedDesc();
        return getPostResponseDtos(email, posts);
    }

    // 구매일자 역순으로 게시물 조회
    public List<PostResponseDto> sortPostsByBuyDate(String email) {
        List<Post> posts = postRepository.findAllByOrderByBuyDateDesc();
        return getPostResponseDtos(email, posts);
    }

    private List<PostResponseDto> getPostResponseDtos(String email, List<Post> posts) {
        Member member = getMemberByEmail(email);
        Long memberId = member.getId();
        return convertPostsToDto(posts, memberId);
    }
}

