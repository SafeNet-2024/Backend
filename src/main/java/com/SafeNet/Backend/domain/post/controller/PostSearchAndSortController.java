package com.SafeNet.Backend.domain.post.controller;

import com.SafeNet.Backend.domain.member.entity.UserDetailsImpl;
import com.SafeNet.Backend.domain.post.dto.PostResponseDto;
import com.SafeNet.Backend.domain.post.entity.Category;
import com.SafeNet.Backend.domain.post.exception.PostException;
import com.SafeNet.Backend.domain.post.service.PostSearchAndSortService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/posts")
@RequiredArgsConstructor
@Tag(name = "Post Query", description = "게시글 특정 조건 조회 및 정렬 API")
public class PostSearchAndSortController {
    private final PostSearchAndSortService postSearchAndSortService;

    @GetMapping("/keyword")
    public ResponseEntity<List<PostResponseDto>> searchByKeyWord(
            @RequestHeader(name = "ACCESS_TOKEN", required = false) String accessToken,
            @RequestHeader(name = "REFRESH_TOKEN", required = false) String refreshToken,
            @RequestParam("keyword") String keyword) {
        String email = getUserEmail();
        List<PostResponseDto> posts = postSearchAndSortService.searchByKeyWord(keyword, email);
        return ResponseEntity.ok(posts);
    }

    @GetMapping("/category")
    public ResponseEntity<List<PostResponseDto>> searchByCategory(
            @RequestHeader(name = "ACCESS_TOKEN", required = false) String accessToken,
            @RequestHeader(name = "REFRESH_TOKEN", required = false) String refreshToken,
            @RequestParam("category") Category category) {
        String email = getUserEmail();
        List<PostResponseDto> posts = postSearchAndSortService.searchByCategory(category, email);
        return ResponseEntity.ok(posts);
    }

    @GetMapping("/sort/created")
    public ResponseEntity<List<PostResponseDto>> sortByCreated(
            @RequestHeader(name = "ACCESS_TOKEN", required = false) String accessToken,
            @RequestHeader(name = "REFRESH_TOKEN", required = false) String refreshToken
    ) {
        String email = getUserEmail();
        List<PostResponseDto> posts = postSearchAndSortService.sortByCreated(email);
        return ResponseEntity.ok(posts);
    }

    @GetMapping("/sort/buyDate")
    public ResponseEntity<List<PostResponseDto>> sortByBuyDate(
            @RequestHeader(name = "ACCESS_TOKEN", required = false) String accessToken,
            @RequestHeader(name = "REFRESH_TOKEN", required = false) String refreshToken
    ) {
        String email = getUserEmail();
        List<PostResponseDto> posts = postSearchAndSortService.sortByBuyDate(email);
        return ResponseEntity.ok(posts);
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

