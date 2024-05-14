package com.SafeNet.Backend.domain.post.api;

import com.SafeNet.Backend.domain.file.domain.File;
import com.SafeNet.Backend.domain.file.domain.FileType;
import com.SafeNet.Backend.domain.file.service.FileStorageService;
import com.SafeNet.Backend.domain.member.domain.Member;
import com.SafeNet.Backend.domain.member.service.MemberService;
import com.SafeNet.Backend.domain.region.domain.Region;
import com.SafeNet.Backend.domain.post.dto.PostRequestDto;
import com.SafeNet.Backend.domain.post.dto.PostResponseDto;
import com.SafeNet.Backend.domain.post.service.PostService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/v1/posts")
@RequiredArgsConstructor
public class PostController {
    private final PostService postService;
    private final FileStorageService fileStorageService;
    private final MemberService memberService;

    @PostMapping(consumes = {"multipart/form-data"})
    public ResponseEntity<String> createPost(@RequestPart("post") String postRequestDtoJson,
                                             @RequestPart("receiptImage") MultipartFile receiptImage,
                                             @RequestPart("productImage") MultipartFile productImage,
                                             @RequestParam("memberId") Long memberId) {
        try {
            // JSON 데이터를 PostRequestDto 객체로 변환
            PostRequestDto postRequestDto = new ObjectMapper().readValue(postRequestDtoJson, PostRequestDto.class);

            // 사용자 정보로 Member 객체 조회
            Member member = memberService.findById(memberId);
            Region region = member.getRegion();

            // 파일 처리
            File receiptFile = fileStorageService.saveFile(receiptImage, FileType.receipt);
            File productFile = fileStorageService.saveFile(productImage, FileType.product_image);

            // 데이터 처리
            postService.createPost(postRequestDto, receiptFile, productFile, member, region);
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
    public PostResponseDto getPostById(@PathVariable("id") Long id) {
        return postService.getPostById(id);
    }
}