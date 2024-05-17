package com.SafeNet.Backend.domain.post.api;

import com.SafeNet.Backend.domain.post.dto.PostRequestDto;
import com.SafeNet.Backend.domain.post.dto.PostResponseDto;
import com.SafeNet.Backend.domain.post.exception.PostException;
import com.SafeNet.Backend.domain.post.service.PostService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
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

    @PostMapping(consumes = {"multipart/form-data"})
    public ResponseEntity<String> createPost(@RequestPart("post") String postRequestDtoJson,
                                             @RequestPart("receiptImage") MultipartFile receiptImage,
                                             @RequestPart("productImage") MultipartFile productImage,
                                             @RequestParam("memberId") Long memberId) throws JsonProcessingException {
        PostRequestDto postRequestDto = new ObjectMapper().readValue(postRequestDtoJson, PostRequestDto.class); // JSON 객체를 PostRequestDto로
        postService.createPost(postRequestDto, receiptImage, productImage, memberId);
        return new ResponseEntity<>("Post created successfully", HttpStatus.CREATED);
    }


    @GetMapping
    public ResponseEntity<?> getAllPosts() {
        List<PostResponseDto> allPosts = postService.getAllPosts();
        if (allPosts.isEmpty()) {
            return new ResponseEntity<>("등록된 게시물이 없습니다.", HttpStatus.OK);
        }
        return new ResponseEntity<>(allPosts, HttpStatus.OK);
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getPostById(@PathVariable("id") Long id) {
        Optional<PostResponseDto> postById = postService.getPostById(id);
        return new ResponseEntity<>(postById, HttpStatus.OK);
    }

    @PatchMapping(value = "/{id}", consumes = {"multipart/form-data"})
    public ResponseEntity<String> updatePost(@PathVariable("id") Long id,
                                             @RequestPart("post") String postRequestDtoJson,
                                             @RequestPart(value = "receiptImage", required = false) MultipartFile receiptImage,
                                             @RequestPart(value = "productImage", required = false) MultipartFile productImage) throws JsonProcessingException {
        PostRequestDto postRequestDto = new ObjectMapper().readValue(postRequestDtoJson, PostRequestDto.class);
        postService.updatePost(id, postRequestDto, receiptImage, productImage);
        return new ResponseEntity<>("Post updated successfully", HttpStatus.OK);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deletePost(@PathVariable("id") Long id) {
        postService.deletePost(id);
        return new ResponseEntity<>("Post deleted successfully", HttpStatus.OK);
    }

    @ExceptionHandler(PostException.class)
    public ResponseEntity<String> handleCustomException(PostException e) {
        return new ResponseEntity<>(e.getMessage(), e.getStatus());
    }
}
