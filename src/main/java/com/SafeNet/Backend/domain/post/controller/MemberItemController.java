package com.SafeNet.Backend.domain.post.controller;

import com.SafeNet.Backend.domain.member.entity.UserDetailsImpl;
import com.SafeNet.Backend.domain.post.dto.PostResponseDto;
import com.SafeNet.Backend.domain.post.exception.PostException;
import com.SafeNet.Backend.domain.post.service.MemberItemService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v2/mypage")
@RequiredArgsConstructor
@Tag(name = "Member Item", description = "사용자가 찜한 게시글, 등록한 게시글 조회 API")
public class MemberItemController {
    private final MemberItemService memberItemService;

    @GetMapping("/my-posts")
    public ResponseEntity<?> getPostsByMemberId(
            @RequestHeader(name = "ACCESS_TOKEN", required = false) String accessToken,
            @RequestHeader(name = "REFRESH_TOKEN", required = false) String refreshToken) {
        String email = getUserEmail();
        List<PostResponseDto> posts = memberItemService.getPostsByMemberId(email);
        if (posts.isEmpty()) {
            return ResponseEntity.ok("사용자가 등록한 게시물이 없습니다.");
        }
        return ResponseEntity.ok(posts);
    }

    @GetMapping("/liked-posts")
    public ResponseEntity<?> getLikedPostsByMemberId(
            @RequestHeader(name = "ACCESS_TOKEN", required = false) String accessToken,
            @RequestHeader(name = "REFRESH_TOKEN", required = false) String refreshToken) {
        String email = getUserEmail();
        List<PostResponseDto> likedPosts = memberItemService.getLikedPostsByMemberId(email);
        if (likedPosts.isEmpty()) {
            return ResponseEntity.ok("등록된 찜이 없습니다.");
        }
        return ResponseEntity.ok(likedPosts);
    }

    @ExceptionHandler(PostException.class)
    public ResponseEntity<String> handleCustomException(PostException e) {
        return ResponseEntity.status(e.getStatus()).body(e.getMessage());
    }

    private static String getUserEmail() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        UserDetailsImpl userDetails = (UserDetailsImpl) principal;
        return userDetails.getUsername();
    }
}
