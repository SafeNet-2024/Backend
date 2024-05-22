package com.SafeNet.Backend.domain.post.api;

import com.SafeNet.Backend.domain.post.dto.PostRequestDto;
import com.SafeNet.Backend.domain.post.dto.PostResponseDto;
import com.SafeNet.Backend.domain.post.exception.PostException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.SafeNet.Backend.domain.post.service.PostService;
import com.fasterxml.jackson.core.JsonProcessingException;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/v1/posts")
@RequiredArgsConstructor
@Tag(name = "Post", description = "Post API")
public class PostController {
    private final PostService postService;
    private final ObjectMapper objectMapper;

    @PostMapping(consumes = {"multipart/form-data"})
    public ResponseEntity<String> createPost(@RequestPart("post") String postRequestDtoJson,
                                             @RequestPart("receiptImage") MultipartFile receiptImage,
                                             @RequestPart("productImage") MultipartFile productImage,
                                             @RequestParam("memberId") Long memberId) throws JsonProcessingException {
        PostRequestDto postRequestDto = objectMapper.readValue(postRequestDtoJson, PostRequestDto.class); // JSON 객체를 PostRequestDto로 변환
        postService.createPost(postRequestDto, receiptImage, productImage, memberId);
        return ResponseEntity.status(HttpStatus.CREATED).body("Post created successfully");
    }

    @GetMapping
    public ResponseEntity<?> getAllPosts() {
        List<PostResponseDto> allPosts = postService.getAllPosts();
        if (allPosts.isEmpty()) {
            return ResponseEntity.ok("등록된 게시물이 없습니다.");
        }
        return ResponseEntity.ok(allPosts);
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getPostById(@PathVariable("id") Long id) {
        Optional<PostResponseDto> postById = postService.getPostById(id);
        if (postById.isPresent()) {
            return ResponseEntity.ok(postById.get());
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Post not found");
        }
    }

    @PatchMapping(value = "/{id}", consumes = {"multipart/form-data"})
    public ResponseEntity<String> updatePost(@PathVariable("id") Long id,
                                             @RequestPart("post") String postRequestDtoJson,
                                             @RequestPart(value = "receiptImage", required = false) MultipartFile receiptImage,
                                             @RequestPart(value = "productImage", required = false) MultipartFile productImage) throws JsonProcessingException {
        PostRequestDto postRequestDto = new ObjectMapper().readValue(postRequestDtoJson, PostRequestDto.class);
        postService.updatePost(id, postRequestDto, receiptImage, productImage);
        return ResponseEntity.ok("Post updated successfully");
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deletePost(@PathVariable("id") Long id) {
        postService.deletePost(id);
        return ResponseEntity.ok("Post deleted successfully");
    }

    @PatchMapping("/{id}/status/trading")
    public ResponseEntity<String> updatePostStatusToTrading(@PathVariable("id") Long id) {
        postService.updatePostStatusToTrading(id);
        return ResponseEntity.ok("Post status updated to trading");
    }

    @PatchMapping("/{id}/status/completed")
    public ResponseEntity<String> updatePostStatusToCompleted(@PathVariable("id") Long id) {
        postService.updatePostStatusToCompleted(id);
        return ResponseEntity.ok("Post status updated to completed");
    }

    @ExceptionHandler(PostException.class)
    public ResponseEntity<String> handleCustomException(PostException e) {
        return ResponseEntity.status(e.getStatus()).body(e.getMessage());
    }

}
