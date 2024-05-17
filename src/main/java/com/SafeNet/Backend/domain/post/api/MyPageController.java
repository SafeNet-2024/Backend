package com.SafeNet.Backend.domain.post.api;

import com.SafeNet.Backend.domain.post.dto.PostResponseDto;
import com.SafeNet.Backend.domain.post.exception.PostException;
import com.SafeNet.Backend.domain.post.service.PostService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/mypage")
@RequiredArgsConstructor
public class MyPageController {
    private final PostService postService;

    @GetMapping("/my-posts")
    public ResponseEntity<?> getPostsByMemberId(@RequestParam("memberId") Long memberId) {
        List<PostResponseDto> posts = postService.getPostsByMemberId(memberId);
        if (posts.isEmpty()) {
            return new ResponseEntity<>("사용자가 등록한 게시물이 없습니다.", HttpStatus.OK);
        }
        return new ResponseEntity<>(posts, HttpStatus.OK);
    }

    @GetMapping("/liked-posts")
    public ResponseEntity<?> getLikedPostsByMemberId(@RequestParam("memberId") Long memberId) {
        List<PostResponseDto> likedPosts = postService.getLikedPostsByMemberId(memberId);
        if (likedPosts.isEmpty()) {
            return new ResponseEntity<>("등록된 찜이 없습니다.", HttpStatus.OK);
        }
        return new ResponseEntity<>(likedPosts, HttpStatus.OK);
    }

    @ExceptionHandler(PostException.class)
    public ResponseEntity<String> handleCustomException(PostException e) {
        return new ResponseEntity<>(e.getMessage(), e.getStatus());
    }
}
