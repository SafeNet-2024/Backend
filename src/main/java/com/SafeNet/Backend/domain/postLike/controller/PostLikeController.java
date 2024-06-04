package com.SafeNet.Backend.domain.postLike.controller;

import com.SafeNet.Backend.domain.member.entity.UserDetailsImpl;
import com.SafeNet.Backend.domain.postLike.service.PostLikeService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v2/posts")
@RequiredArgsConstructor
@Tag(name = "PostLike", description = "Post Like API")
public class PostLikeController {
    private final PostLikeService postLikeService;

    @PostMapping("/{postId}/like")
    public ResponseEntity<String> likePost(
            @PathVariable("postId") Long postId,
            @RequestHeader(name = "ACCESS_TOKEN", required = false) String accessToken,
            @RequestHeader(name = "REFRESH_TOKEN", required = false) String refreshToken
    ) {
        String email = getUserEmail();
        boolean liked = postLikeService.likePost(postId, email);
        if (liked) {
            return ResponseEntity.ok("Post liked successfully");
        } else {
            return ResponseEntity.ok("Post unliked successfully");
        }
    }
    private static String getUserEmail() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        UserDetailsImpl userDetails = (UserDetailsImpl) principal;
        return userDetails.getUsername();
    }
}
