package com.SafeNet.Backend.domain.post.service;

import com.SafeNet.Backend.domain.file.domain.File;
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

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class PostService {
    private final PostRepository postRepository;
    private final FileStorageService fileStorageService;

    @Transactional
    public void createPost(PostRequestDto postRequestDto, File receiptFile, File productFile, Member member, Region region) {
        // 문자열로 받은 날짜 데이터를 LocalDate로 파싱
        LocalDate parsedBuyDate = LocalDate.parse(postRequestDto.getBuyDate(), DateTimeFormatter.ISO_LOCAL_DATE);

        // Post 객체 생성 및 저장
        Post post = Post.builder()
                .title(postRequestDto.getTitle())
                .category(postRequestDto.getCategory())
                .cost(postRequestDto.getCost())
                .count(postRequestDto.getCount())
                .buyDate(parsedBuyDate)
                .contents(postRequestDto.getContents())
                .member(member)
                .region(region)
                .fileList(Arrays.asList(receiptFile, productFile)) // 영수증과 상품 이미지 추가
                .build();
        postRepository.save(post);
    }


    public List<PostResponseDto> getAllPosts() {
        List<Post> posts = postRepository.findAll();
        return posts.stream().map(post -> PostResponseDto.builder()
                .postId(post.getId())
                .title(post.getTitle())
                .likeCount(post.getPostLikeList().size()) // 좋아요 개수가 0이면 빈 하트, 1이면 채운 하트
                .imageUrl(post.getFileList().isEmpty() ? null : post.getFileList().get(1).getFileUrl()) // 상품 이미지 가져오기
                .build()).collect(Collectors.toList());
    }

    public PostResponseDto getPostById(Long id) {
        return postRepository.findById(id).map(post -> PostResponseDto.builder()
                .postId(post.getId())
                .title(post.getTitle())
                .writer(post.getMember().getName())
                .contents(post.getContents())
                .imageUrl(post.getFileList().isEmpty() ? null : post.getFileList().get(1).getFileUrl())
                .price(post.getCost())
                .count(post.getCount())
                .buyDate(post.getBuyDate().toString())
                .category(post.getCategory()).build()).orElseThrow(() -> new RuntimeException("Post not found with id: " + id));
    }
}
