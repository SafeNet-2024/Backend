package com.SafeNet.Backend.domain.post.service;

import com.SafeNet.Backend.domain.member.entity.Member;
import com.SafeNet.Backend.domain.member.repository.MemberRepository;
import com.SafeNet.Backend.domain.post.dto.PostResponseDto;
import com.SafeNet.Backend.domain.post.entity.Category;
import com.SafeNet.Backend.domain.post.entity.Post;
import com.SafeNet.Backend.domain.post.exception.PostException;
import com.SafeNet.Backend.domain.post.repository.PostRepository;
import com.SafeNet.Backend.domain.postLike.repository.PostLikeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class PostSearchAndSortService {
    private final PostRepository postRepository;
    private final MemberRepository memberRepository;
    private final PostLikeRepository postLikeRepository;


    public List<PostResponseDto> searchByKeyWord(String keyword, String email) { // 글 제목 또는 설명으로 검색
        Member member = memberRepository.findByEmail(email).orElseThrow(() -> new PostException("Member not found with email: " + email, HttpStatus.NOT_FOUND));
        try {
            Long memberId = member.getId();
            List<Post> posts = postRepository.findByTitleContainingOrContentsContaining(keyword, keyword);
            return posts.stream().map(post -> {
                boolean isLikedByCurrentUser = postLikeRepository.existsByPostIdAndMemberId(post.getId(), memberId);
                return convertToDto(post, isLikedByCurrentUser);
            }).collect(Collectors.toList());
        } catch (Exception e) {
            throw new PostException("Failed to retrieve posts by KeyWord", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    public List<PostResponseDto> searchByCategory(Category category, String email) { // 카테고리로 검색
        Member member = memberRepository.findByEmail(email).orElseThrow(() -> new PostException("Member not found with email: " + email, HttpStatus.NOT_FOUND));
        try {
            Long memberId = member.getId();
            List<Post> posts = postRepository.findByCategory(category);
            return posts.stream().map(post -> {
                boolean isLikedByCurrentUser = postLikeRepository.existsByPostIdAndMemberId(post.getId(), memberId);
                return convertToDto(post, isLikedByCurrentUser);
            }).collect(Collectors.toList());
        } catch (Exception e) {
            throw new PostException("Failed to retrieve posts by Category", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    public List<PostResponseDto> sortByCreated(String email) { // 글 등록된 순(최신순)으로 게시글을 정렬하는 로직
        Member member = memberRepository.findByEmail(email).orElseThrow(() -> new PostException("Member not found with email: " + email, HttpStatus.NOT_FOUND));
        try {
            Long memberId = member.getId();
            List<Post> posts = postRepository.findAllByOrderByCreatedDesc();
            return posts.stream().map(post -> {
                boolean isLikedByCurrentUser = postLikeRepository.existsByPostIdAndMemberId(post.getId(), memberId);
                return convertToDto(post, isLikedByCurrentUser);
            }).collect(Collectors.toList());
        } catch (Exception e) {
            throw new PostException("Failed to retrieve posts by created date", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    public List<PostResponseDto> sortByBuyDate(String email) { // 구매날짜 최신순으로 게시글을 정렬하는 로직
        Member member = memberRepository.findByEmail(email).orElseThrow(() -> new PostException("Member not found with email: " + email, HttpStatus.NOT_FOUND));
        try {
            Long memberId = member.getId();
            List<Post> posts = postRepository.findAllByOrderByBuyDateDesc();
            return posts.stream().map(post -> {
                boolean isLikedByCurrentUser = postLikeRepository.existsByPostIdAndMemberId(post.getId(), memberId);
                return convertToDto(post, isLikedByCurrentUser);
            }).collect(Collectors.toList());
        } catch (Exception e) {
            throw new PostException("Failed to retrieve posts by buy date", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    private PostResponseDto convertToDto(Post post, boolean isLikedByCurrentUser) {
        return PostResponseDto.builder()
                .postId(post.getId())
                .title(post.getTitle())
                .likeCount(isLikedByCurrentUser)
                .productImageUrl(post.getFileList().isEmpty() ? null : post.getFileList().get(1).getFileUrl())
                .count(post.getCount())
                .cost(post.getCost())
                .build();
    }
}
