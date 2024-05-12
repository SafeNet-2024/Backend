package com.SafeNet.Backend.domain.post.service;

import com.SafeNet.Backend.domain.file.domain.FileType;
import com.SafeNet.Backend.domain.file.domain.File;
import com.SafeNet.Backend.domain.file.repository.FileRepository;
import com.SafeNet.Backend.domain.file.service.FileStorageService;
import com.SafeNet.Backend.domain.member.domain.Member;
import com.SafeNet.Backend.domain.post.domain.Post;
import com.SafeNet.Backend.domain.post.dto.PostRequestDto;
import com.SafeNet.Backend.domain.post.dto.PostResponseDto;
import com.SafeNet.Backend.domain.post.repository.PostRepository;
import com.SafeNet.Backend.domain.region.domain.Region;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class PostService {
    private final PostRepository postRepository;
    private final FileStorageService fileStorageService;

    @Transactional
    public void createPost(PostRequestDto postRequestDto, Member member, Region region) {
        // 문자열로 받은 날짜 데이터를 LocalDate로 파싱
        LocalDate parsedBuyDate = LocalDate.parse(postRequestDto.getBuyDate(), DateTimeFormatter.ISO_LOCAL_DATE);

        // 파일 저장
        File receiptFile = fileStorageService.saveFile(postRequestDto.getReceiptImage(), FileType.receipt);
        File productFile = fileStorageService.saveFile(postRequestDto.getProductImage(), FileType.product_image);

        // Post 객체 생성 및 저장
        Post post = Post.builder()
                .title(postRequestDto.getTitle())
                .category(postRequestDto.getCategory())
                .cost(postRequestDto.getCost())
                .count(postRequestDto.getCount())
                .buyDate(parsedBuyDate)
                .contents(postRequestDto.getContents())
                .member_post(member)
                .region_post(region)
                .fileList(Arrays.asList(receiptFile, productFile)) // 영수증과 상품 이미지 추가
                .build();
        postRepository.save(post);
    }


    public List<PostResponseDto> getAllPosts() {
        List<Post> posts = postRepository.findAll();
        return posts.stream().map(post -> PostResponseDto.builder()
                .postId(post.getId())
                .title(post.getTitle())
                .likeCount(post.getLikesList().size()) // 좋아요 개수가 0이면 빈 하트, 1이면 채운 하트
                .imageUrl(post.getFileList().isEmpty() ? null : post.getFileList().get(0).getFileUrl())
                .build()).collect(Collectors.toList());
    }

    public PostResponseDto getPostById(Long id) {
        return postRepository.findById(id).map(post -> PostResponseDto.builder()
                .postId(post.getId())
                .title(post.getTitle())
                .writer(post.getMember_post().getName())
                .contents(post.getContents())
                .imageUrl(post.getFileList().isEmpty() ? null : post.getFileList().get(0).getFileUrl())
                .price(post.getCost())
                .count(post.getCount())
                .category(post.getCategory()).build()).orElseThrow(() -> new RuntimeException("Post not found with id: " + id));
    }
}
