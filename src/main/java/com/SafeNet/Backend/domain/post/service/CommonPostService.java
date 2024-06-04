package com.SafeNet.Backend.domain.post.service;

import com.SafeNet.Backend.domain.member.entity.Member;
import com.SafeNet.Backend.domain.member.repository.MemberRepository;
import com.SafeNet.Backend.domain.post.dto.PostResponseDto;
import com.SafeNet.Backend.domain.post.entity.Category;
import com.SafeNet.Backend.domain.post.entity.Post;
import com.SafeNet.Backend.domain.post.exception.PostException;
import com.SafeNet.Backend.domain.post.repository.PostRepository;
import com.SafeNet.Backend.domain.postLike.repository.PostLikeRepository;
import com.SafeNet.Backend.domain.post.util.PostDtoConverter;
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

    public List<PostResponseDto> getAllPosts(String email) {
        Member member = getMemberByEmail(email);
        Long memberId = member.getId();
        List<Post> posts = postRepository.findAll();
        return convertPostsToDto(posts, memberId);
    }

    public List<PostResponseDto> getPostsByMemberId(String email) {
        Member member = getMemberByEmail(email);
        Long memberId = member.getId();
        List<Post> posts = postRepository.findByMember_Id(memberId);
        return convertPostsToDto(posts, memberId);
    }

    public List<PostResponseDto> getLikedPostsByMemberId(String email) {
        Member member = getMemberByEmail(email);
        Long memberId = member.getId();
        List<Post> posts = postLikeRepository.findByMember_Id(memberId).stream()
                .map(postLike -> postLike.getPost())
                .collect(Collectors.toList());
        return convertPostsToDto(posts, memberId);
    }

    public List<PostResponseDto> searchPostsByKeyword(String keyword, String email) {
        Member member = getMemberByEmail(email);
        Long memberId = member.getId();
        List<Post> posts = postRepository.findByTitleContainingOrContentsContaining(keyword, keyword);
        return convertPostsToDto(posts, memberId);
    }

    public List<PostResponseDto> searchPostsByCategory(Category category, String email) {
        Member member = getMemberByEmail(email);
        Long memberId = member.getId();
        List<Post> posts = postRepository.findByCategory(category);
        return convertPostsToDto(posts, memberId);
    }

    public List<PostResponseDto> sortPostsByCreated(String email) {
        Member member = getMemberByEmail(email);
        Long memberId = member.getId();
        List<Post> posts = postRepository.findAllByOrderByCreatedDesc();
        return convertPostsToDto(posts, memberId);
    }

    public List<PostResponseDto> sortPostsByBuyDate(String email) {
        Member member = getMemberByEmail(email);
        Long memberId = member.getId();
        List<Post> posts = postRepository.findAllByOrderByBuyDateDesc();
        return convertPostsToDto(posts, memberId);
    }
}

