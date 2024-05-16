package com.SafeNet.Backend.domain.post.service;

import com.SafeNet.Backend.domain.file.domain.File;
import com.SafeNet.Backend.domain.file.domain.FileType;
import com.SafeNet.Backend.domain.file.service.FileStorageService;
import com.SafeNet.Backend.domain.member.domain.Member;
import com.SafeNet.Backend.domain.member.service.MemberService;
import com.SafeNet.Backend.domain.post.domain.Post;
import com.SafeNet.Backend.domain.post.domain.PostStatus;
import com.SafeNet.Backend.domain.post.dto.PostRequestDto;
import com.SafeNet.Backend.domain.post.dto.PostResponseDto;
import com.SafeNet.Backend.domain.post.repository.PostRepository;
import com.SafeNet.Backend.domain.region.domain.Region;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

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
    private final MemberService memberService;
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy년 M월 d일");

    @Transactional
    public void createPost(PostRequestDto postRequestDto, MultipartFile receiptImage, MultipartFile productImage, Long memberId) {
        // 문자열로 받은 날짜 데이터를 LocalDate로 파싱
        LocalDate parsedBuyDate = LocalDate.parse(postRequestDto.getBuyDate(), formatter);

        // 사용자 정보로 Member 객체 조회
        Member member = memberService.findById(memberId);
        Region region = member.getRegion();

        // 파일 처리
        File receiptFile = fileStorageService.saveFile(receiptImage, FileType.receipt);
        File productFile = fileStorageService.saveFile(productImage, FileType.product_image);


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
                .likeCount(post.getPostLikeList().size())
                .imageUrl(post.getFileList().isEmpty() ? null : post.getFileList().get(1).getFileUrl())
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

    @Transactional
    public void updatePost(Long id, PostRequestDto postRequestDto, MultipartFile receiptImage, MultipartFile productImage) {
        Post existingPost = postRepository.findById(id).orElseThrow(() -> new RuntimeException("Post not found with id: " + id));

        // status가 AVAILABLE이면 업데이트 가능
        if (existingPost.getStatus() != PostStatus.AVAILABLE) {
            throw new RuntimeException("Cannot update post with id: " + id + " because its status is not 'AVAILABLE'.");
        }

        // 문자열로 받은 날짜 데이터를 LocalDate로 파싱
        LocalDate parsedBuyDate = LocalDate.parse(postRequestDto.getBuyDate(), formatter);

        // 파일 처리
        List<File> fileList = existingPost.getFileList();

        if (receiptImage != null && !receiptImage.isEmpty()) {
            fileStorageService.deleteFile(fileList.get(0)); // 기존 영수증 파일 삭제
            File receiptFile = fileStorageService.saveFile(receiptImage, FileType.receipt); // 새로운 영수증 파일 추가
            fileList.set(0, receiptFile); // 기존 post가 새로운 파일을 담도록 한다.
        }

        if (productImage != null && !productImage.isEmpty()) {
            fileStorageService.deleteFile(fileList.get(1)); // 기존 상품 이미지 파일 삭제
            File productFile = fileStorageService.saveFile(productImage, FileType.product_image); // 새로운 상품 이미지 파일 로컬에 추가
            fileList.set(1, productFile); // 기존 post가 새로운 파일을 담도록 한다.
        }

        // Post 객체 업데이트 (빌더 패턴 사용, id, status, member, region은 변경하지 않음)
        Post updatedPost = existingPost.updatePost(postRequestDto, parsedBuyDate, fileList);
        postRepository.save(updatedPost);
    }

    @Transactional
    public void deletePost(Long id) {
        Post post = postRepository.findById(id).orElseThrow(() -> new RuntimeException("Post not found with id: " + id));

        // status가 AVAILABLE이면 업데이트 가능
        if (post.getStatus() != PostStatus.AVAILABLE) {
            throw new RuntimeException("Cannot update post with id: " + id + " because its status is not 'AVAILABLE'.");
        }

        post.getFileList().forEach(fileStorageService::deleteFile);
        postRepository.delete(post); // 게시글 삭제
    }
}
