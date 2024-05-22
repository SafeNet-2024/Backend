package com.SafeNet.Backend.domain.postLike.api;

import com.SafeNet.Backend.domain.postLike.service.PostLikeService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/posts")
@RequiredArgsConstructor
@Tag(name = "PostLike", description = "Post Like API")
public class PostLikeController {
    private final PostLikeService postLikeService;

    @PostMapping("/{id}/like")
    public ResponseEntity<String> likePost(@PathVariable("id") Long postId, @RequestParam("memberId") Long memberId) {
        boolean liked = postLikeService.likePost(postId, memberId);
        if (liked) {
            return ResponseEntity.ok("Post liked successfully");
        } else {
            return ResponseEntity.ok("Post unliked successfully");
        }
    }
}
