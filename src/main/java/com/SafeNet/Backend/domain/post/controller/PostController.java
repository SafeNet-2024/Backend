package com.SafeNet.Backend.domain.post.controller;

import com.SafeNet.Backend.domain.member.entity.UserDetailsImpl;
import com.SafeNet.Backend.domain.post.dto.PostRequestDto;
import com.SafeNet.Backend.domain.post.dto.PostResponseDto;
import com.SafeNet.Backend.domain.post.exception.PostException;
import com.SafeNet.Backend.domain.post.service.PostService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.JsonProcessingException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/v2/posts")
@RequiredArgsConstructor
@Tag(name = "Post", description = "게시물 CRUD API")
public class PostController {
    private final PostService postService;
    private final ObjectMapper objectMapper;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "게시물 생성", description = "이미지가 들어있는 글 등록할 때 사용하는 API")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "게시물이 성공적으로 생성되었습니다"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청입니다")
    })
    public ResponseEntity<String> createPost(
            @RequestHeader(name = "ACCESS_TOKEN", required = false) String accessToken,
            @RequestHeader(name = "REFRESH_TOKEN", required = false) String refreshToken,
            @RequestPart(value = "post") String postRequestDtoJson,
            @RequestPart(value = "receiptImage") MultipartFile receiptImage,
            @RequestPart(value = "productImage") MultipartFile productImage) throws JsonProcessingException {
        PostRequestDto postRequestDto = objectMapper.readValue(postRequestDtoJson, PostRequestDto.class); // JSON 객체를 PostRequestDto로 변환
        String email = getUserEmail();
        postService.createPost(postRequestDto, receiptImage, productImage, email);
        return ResponseEntity.status(HttpStatus.CREATED).body("Post created successfully");
    }

    @GetMapping
    @Operation(summary = "게시물 리스트 불러오기", description = "전체 게시물을 리스트로 볼 때 사용하는 API")
    public ResponseEntity<?> getPosts(
            @RequestHeader(name = "ACCESS_TOKEN", required = false) String accessToken,
            @RequestHeader(name = "REFRESH_TOKEN", required = false) String refreshToken
    ) {
        String email = getUserEmail();
        List<PostResponseDto> allPosts = postService.getAllPosts(email);
        if (allPosts.isEmpty()) {
            return ResponseEntity.ok("등록된 게시물이 없습니다.");
        }
        return ResponseEntity.ok(allPosts);
    }

    @GetMapping("/{postId}")
    @Operation(summary = "특정 게시물 불러오기", description = "특정 게시물 상세보기 할 때 사용하는 API")
    public ResponseEntity<?> getPostById(
            @RequestHeader(name = "ACCESS_TOKEN", required = false) String accessToken,
            @RequestHeader(name = "REFRESH_TOKEN", required = false) String refreshToken,
            @PathVariable("postId") Long id) {
        Optional<PostResponseDto> postById = postService.getPostById(id);
        if (postById.isPresent()) {
            return ResponseEntity.ok(postById.get());
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Post not found");
        }
    }

    @PatchMapping(value = "/{postId}", consumes = {"multipart/form-data"})
    @Operation(summary = "특정 게시물 업데이트하기", description = "특정 게시물 업데이트 할 때 사용하는 API")
    public ResponseEntity<String> updatePost(
            @RequestHeader(name = "ACCESS_TOKEN", required = false) String accessToken,
            @RequestHeader(name = "REFRESH_TOKEN", required = false) String refreshToken,
            @PathVariable("postId") Long id,
            @RequestPart("post") String postRequestDtoJson,
            @RequestPart(value = "receiptImage", required = false) MultipartFile receiptImage,
            @RequestPart(value = "productImage", required = false) MultipartFile productImage) throws JsonProcessingException {
        PostRequestDto postRequestDto = new ObjectMapper().readValue(postRequestDtoJson, PostRequestDto.class);
        String email = getUserEmail();
        postService.updatePost(id, postRequestDto, receiptImage, productImage, email);
        return ResponseEntity.ok("Post updated successfully");
    }

    @DeleteMapping("/{postId}")
    @Operation(summary = "특정 게시물 삭제하기", description = "특정 게시물 삭제 할 때 사용하는 API")
    public ResponseEntity<String> deletePost(
            @RequestHeader(name = "ACCESS_TOKEN", required = false) String accessToken,
            @RequestHeader(name = "REFRESH_TOKEN", required = false) String refreshToken,
            @PathVariable("postId") Long id) {
        String email = getUserEmail();
        postService.deletePost(id, email);
        return ResponseEntity.ok("Post deleted successfully");
    }

    @PatchMapping("/{postId}/status/trading")
    @Operation(summary = "게시물 상태 거래중으로 변경", description = "채팅방에서 '거래중'으로 변경할 때 사용하는 API")
    public ResponseEntity<String> updatePostStatusToTrading(
            @RequestHeader(name = "ACCESS_TOKEN", required = false) String accessToken,
            @RequestHeader(name = "REFRESH_TOKEN", required = false) String refreshToken,
            @PathVariable("postId") Long id) {
        postService.updatePostStatusToTrading(id);
        return ResponseEntity.ok("Post status updated to trading");
    }

    @PatchMapping("/{postId}/status/completed")
    @Operation(summary = "게시물 상태 거래완료로 변경", description = "채팅방에서 '거래완료'로 변경할 때 사용하는 API")
    public ResponseEntity<String> updatePostStatusToCompleted(
            @RequestHeader(name = "ACCESS_TOKEN", required = false) String accessToken,
            @RequestHeader(name = "REFRESH_TOKEN", required = false) String refreshToken,
            @PathVariable("postId") Long id) {
        postService.updatePostStatusToCompleted(id);
        return ResponseEntity.ok("Post status updated to completed");
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