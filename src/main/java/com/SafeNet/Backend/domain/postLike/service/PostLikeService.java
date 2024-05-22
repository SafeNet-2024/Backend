package com.SafeNet.Backend.domain.postLike.service;

import com.SafeNet.Backend.domain.member.entity.Member;
import com.SafeNet.Backend.domain.member.service.MemberService;
import com.SafeNet.Backend.domain.post.entity.Post;
import com.SafeNet.Backend.domain.post.exception.PostException;
import com.SafeNet.Backend.domain.post.repository.PostRepository;
import com.SafeNet.Backend.domain.postLike.entity.PostLike;
import com.SafeNet.Backend.domain.postLike.repository.PostLikeRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class PostLikeService {
    private final PostLikeRepository postLikeRepository;
    private final PostRepository postRepository;
    private final MemberService memberService;

    @Transactional
    public boolean likePost(Long postId, Long memberId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new PostException("Post not found", HttpStatus.NOT_FOUND));
        Member member = memberService.findById(memberId);
        Optional<PostLike> existingLike = postLikeRepository.findByPostIdAndMemberId(postId, memberId);

        if (existingLike.isPresent()) {
            postLikeRepository.delete(existingLike.get());
            return false; // 좋아요 취소
        } else {
            PostLike postLike = PostLike.builder()
                    .post(post)
                    .member(member)
                    .build();
            postLikeRepository.save(postLike);
            return true; // 좋아요 추가
        }
    }
}
