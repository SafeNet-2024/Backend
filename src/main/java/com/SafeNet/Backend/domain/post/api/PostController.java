package com.SafeNet.Backend.domain.post.api;

import com.SafeNet.Backend.domain.file.domain.File;
import com.SafeNet.Backend.domain.file.domain.FileType;
import com.SafeNet.Backend.domain.file.service.FileStorageService;
import com.SafeNet.Backend.domain.member.domain.Member;
import com.SafeNet.Backend.domain.member.service.MemberService;
import com.SafeNet.Backend.domain.region.domain.Region;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import com.SafeNet.Backend.domain.post.dto.PostRequestDto;
import com.SafeNet.Backend.domain.post.dto.PostResponseDto;
import com.SafeNet.Backend.domain.post.service.PostService;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/posts")
@RequiredArgsConstructor
public class PostController {
    private final PostService postService;
    private final FileStorageService fileStorageService;
    private final MemberService memberService;

    @PostMapping
    public void createPost(@ModelAttribute PostRequestDto postRequestDto,
                           @RequestParam("receiptImage") MultipartFile receiptImage,
                           @RequestParam("productImage") MultipartFile productImage,
                           @RequestParam("memberId") Long memberId) throws IOException {

        // 파일 처리
        File receiptFile = fileStorageService.saveFile(receiptImage, FileType.receipt);
        File productFile = fileStorageService.saveFile(productImage, FileType.product_image);

        Member member = memberService.findById(memberId);
        Region region = member.getRegion();

        // 데이터 처리
        postService.createPost(postRequestDto, receiptFile, productFile, member, region);
    }

    @GetMapping
    public List<PostResponseDto> getAllPosts() {
        return postService.getAllPosts();
    }

    @GetMapping("/{id}")
    public PostResponseDto getPostById(@PathVariable Long id) {
        return postService.getPostById(id);
    }
}
