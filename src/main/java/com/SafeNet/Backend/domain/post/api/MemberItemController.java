package com.SafeNet.Backend.domain.post.api;

import com.SafeNet.Backend.domain.post.dto.PostResponseDto;
import com.SafeNet.Backend.domain.post.exception.PostException;
import com.SafeNet.Backend.domain.post.service.MemberItemService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/mypage")
@RequiredArgsConstructor
@Tag(name = "Member Item", description = "사용자가 찜한 게시글, 등록한 게시글 조회 API")
public class MemberItemController {
    private final MemberItemService memberItemService;

    @GetMapping("/my-posts")
    public ResponseEntity<?> getPostsByMemberId(@RequestParam("memberId") Long memberId) {
        List<PostResponseDto> posts = memberItemService.getPostsByMemberId(memberId);
        if (posts.isEmpty()) {
            return ResponseEntity.ok("사용자가 등록한 게시물이 없습니다.");
        }
        return ResponseEntity.ok(posts);
    }

    @GetMapping("/liked-posts")
    public ResponseEntity<?> getLikedPostsByMemberId(@RequestParam("memberId") Long memberId) {
        List<PostResponseDto> likedPosts = memberItemService.getLikedPostsByMemberId(memberId);
        if (likedPosts.isEmpty()) {
            return ResponseEntity.ok("등록된 찜이 없습니다.");
        }
        return ResponseEntity.ok(likedPosts);
    }
    @ExceptionHandler(PostException.class)
    public ResponseEntity<String> handleCustomException(PostException e) {
        return ResponseEntity.status(e.getStatus()).body(e.getMessage());
    }
}