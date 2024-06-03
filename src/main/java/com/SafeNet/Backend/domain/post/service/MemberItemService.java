package com.SafeNet.Backend.domain.post.service;

import com.SafeNet.Backend.domain.member.entity.Member;
import com.SafeNet.Backend.domain.member.repository.MemberRepository;
import com.SafeNet.Backend.domain.post.dto.PostResponseDto;
import com.SafeNet.Backend.domain.post.entity.Post;
import com.SafeNet.Backend.domain.post.exception.PostException;
import com.SafeNet.Backend.domain.post.repository.PostRepository;
import com.SafeNet.Backend.domain.postLike.entity.PostLike;
import com.SafeNet.Backend.domain.postLike.repository.PostLikeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

import static java.lang.Boolean.FALSE;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class MemberItemService {
    private final PostRepository postRepository;
    private final PostLikeRepository postLikeRepository;
    private final MemberRepository memberRepository;

    public List<PostResponseDto> getPostsByMemberId(String email) {
        Member member = memberRepository.findByEmail(email).orElseThrow(() -> new PostException("Member not found", HttpStatus.NOT_FOUND));
        try {
            Long memberId = member.getId();
            List<Post> posts = postRepository.findByMember_Id(member.getId());
            return posts.stream().map(post -> {
                boolean isLikedByCurrentUser = postLikeRepository.existsByPostIdAndMemberId(post.getId(), memberId);
                return convertToDto(post, isLikedByCurrentUser);
            }).collect(Collectors.toList());
        } catch (Exception e) {
            throw new PostException("Failed to retrieve posts by memberId", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    public List<PostResponseDto> getLikedPostsByMemberId(String email) {
        Member member = memberRepository.findByEmail(email).orElseThrow(() -> new PostException("Member not found", HttpStatus.NOT_FOUND));
        try {
            List<PostLike> postLikes = postLikeRepository.findByMember_Id(member.getId());
            return postLikes.stream().map(postLike -> this.convertToDto(postLike.getPost(), FALSE)).collect(Collectors.toList());
        } catch (Exception e) {
            throw new PostException("Failed to retrieve liked posts by memberId", HttpStatus.INTERNAL_SERVER_ERROR);
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
