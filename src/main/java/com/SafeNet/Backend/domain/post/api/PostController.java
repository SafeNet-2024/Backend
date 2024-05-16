package com.SafeNet.Backend.domain.post.api;

import com.SafeNet.Backend.domain.post.dto.PostRequestDto;
import com.SafeNet.Backend.domain.post.dto.PostResponseDto;
import com.SafeNet.Backend.domain.post.service.PostService;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/v1/posts")
@RequiredArgsConstructor
@Tag(name = "Post", description = "Post API")
public class PostController {
    private final PostService postService;

    @PostMapping(consumes = {"multipart/form-data"})
    public ResponseEntity<String> createPost(@RequestPart("post") String postRequestDtoJson,
                                             @RequestPart("receiptImage") MultipartFile receiptImage,
                                             @RequestPart("productImage") MultipartFile productImage,
                                             @RequestParam("memberId") Long memberId) {
        try {
            PostRequestDto postRequestDto = new ObjectMapper().readValue(postRequestDtoJson, PostRequestDto.class); // JSON 객체를 PostRequestDto로 변환
            postService.createPost(postRequestDto, receiptImage, productImage, memberId);
            return new ResponseEntity<>("Post created successfully", HttpStatus.CREATED);
        } catch (Exception e) {
            return new ResponseEntity<>("Failed to create post: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping
    public List<PostResponseDto> getAllPosts() {
        return postService.getAllPosts();
    }

    @GetMapping("/{id}")
    public ResponseEntity<PostResponseDto> getPostById(@PathVariable("id") Long id) {
        try {
            return new ResponseEntity<>(postService.getPostById(id), HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
        }
    }

    @PatchMapping(value = "/{id}", consumes = {"multipart/form-data"})
    public ResponseEntity<String> updatePost(@PathVariable("id") Long id,
                                             @RequestPart("post") String postRequestDtoJson,
                                             @RequestPart(value = "receiptImage", required = false) MultipartFile receiptImage,
                                             @RequestPart(value = "productImage", required = false) MultipartFile productImage) {
        try {
            PostRequestDto postRequestDto = new ObjectMapper().readValue(postRequestDtoJson, PostRequestDto.class);
            postService.updatePost(id, postRequestDto, receiptImage, productImage);
            return new ResponseEntity<>("Post updated successfully", HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>("Failed to update post: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deletePost(@PathVariable("id") Long id) {
        try {
            postService.deletePost(id);
            return new ResponseEntity<>("Post deleted successfully", HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>("Failed to delete post: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
