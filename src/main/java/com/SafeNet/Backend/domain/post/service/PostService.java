package com.SafeNet.Backend.domain.post.service;

import com.SafeNet.Backend.domain.file.entity.File;
import com.SafeNet.Backend.domain.file.entity.FileType;
import com.SafeNet.Backend.domain.file.service.FileStorageService;
import com.SafeNet.Backend.domain.file.service.S3Service;
import com.SafeNet.Backend.domain.member.entity.Member;
import com.SafeNet.Backend.domain.member.service.MemberService;
import com.SafeNet.Backend.domain.post.entity.Post;
import com.SafeNet.Backend.domain.post.entity.PostStatus;
import com.SafeNet.Backend.domain.post.dto.PostRequestDto;
import com.SafeNet.Backend.domain.post.dto.PostResponseDto;
import com.SafeNet.Backend.domain.post.exception.PostException;
import com.SafeNet.Backend.domain.post.repository.PostRepository;
import com.SafeNet.Backend.domain.region.entity.Region;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class PostService {
    private final PostRepository postRepository;
    private final FileStorageService fileStorageService;
    private final MemberService memberService;
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy년 M월 d일");
    private final S3Service s3Service;

    @Transactional
    public void createPost(PostRequestDto postRequestDto, MultipartFile receiptImage, MultipartFile productImage, Long memberId) {
        try {
            // 문자열로 받은 날짜 데이터를 LocalDate로 파싱
            LocalDate parsedBuyDate = LocalDate.parse(postRequestDto.getBuyDate(), formatter);

            // 사용자 정보로 Member 객체 조회
            Member member = memberService.findById(memberId);
            Region region = member.getRegion();

            // S3에 파일 업로드 및 URL 반환
            String receiptImageUrl = s3Service.upload("receiptImage", receiptImage.getOriginalFilename(), receiptImage);
            String productImageUrl = s3Service.upload("productImage", productImage.getOriginalFilename(), productImage);

            // URL을 통해 파일 엔티티 저장
            File receiptFile = fileStorageService.saveFile(receiptImageUrl, FileType.receipt);
            File productFile = fileStorageService.saveFile(productImageUrl, FileType.product_image);

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
                    .fileList(Arrays.asList(receiptFile, productFile))
                    .build();
            postRepository.save(post);
        } catch (Exception e) {
            throw new PostException("Failed to create post", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    public List<PostResponseDto> getAllPosts() {
        try {
            List<Post> posts = postRepository.findAll();
            return posts.stream().map(this::convertToDto).collect(Collectors.toList());
        } catch (Exception e) {
            throw new PostException("Failed to retrieve posts", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    public Optional<PostResponseDto> getPostById(Long id) {
        return Optional.ofNullable(postRepository.findById(id).map(post -> PostResponseDto.builder()
                        .postId(post.getId())
                        .title(post.getTitle())
                        .writer(post.getMember().getName())
                        .contents(post.getContents())
                        .receiptImageUrl(post.getFileList().isEmpty() ? null : post.getFileList().get(0).getFileUrl())
                        .productImageUrl(post.getFileList().isEmpty() ? null : post.getFileList().get(1).getFileUrl())
                        .cost(post.getCost())
                        .count(post.getCount())
                        .buyDate(post.getBuyDate().toString())
                        .category(post.getCategory())
                        .likeCount(post.getPostLikeList().size())
                        .build())
                .orElseThrow(() -> new PostException("Post not found with id: " + id, HttpStatus.NOT_FOUND)));
    }

    @Transactional
    public void updatePost(Long id, PostRequestDto postRequestDto, MultipartFile receiptImage, MultipartFile productImage) {
        Post existingPost = postRepository.findById(id).orElseThrow(() -> new PostException("Post not found with id: " + id, HttpStatus.NOT_FOUND));
        try {
            if (existingPost.getPostStatus() != PostStatus.거래가능) {
                throw new PostException("Cannot update post with id: " + id + " because its status is not 'AVAILABLE'.", HttpStatus.BAD_REQUEST);
            }

            LocalDate parsedBuyDate = LocalDate.parse(postRequestDto.getBuyDate(), formatter);
            List<File> fileList = existingPost.getFileList();

            if (receiptImage != null && !receiptImage.isEmpty()) {
                // S3와 File 엔티티에 파일 삭제
                s3Service.delete(fileList.get(0).getFileUrl());
                fileStorageService.deleteFile(fileList.get(0));

                // S3와 File 엔티티에 파일 생성
                String receiptImageUrl = s3Service.upload("receiptImage", receiptImage.getOriginalFilename(), receiptImage);
                File receiptFile = fileStorageService.saveFile(receiptImageUrl, FileType.receipt);

                // 기존 fileList의 첫 번째 요소를 새로 업로드된 영수증 이미지 파일로 대체
                fileList.set(0, receiptFile);
            }
            if (productImage != null && !productImage.isEmpty()) {
                // S3와 File 엔티티에 파일 삭제
                s3Service.delete(fileList.get(1).getFileUrl());
                fileStorageService.deleteFile(fileList.get(1));

                // S3와 File 엔티티에 파일 생성
                String productImageUrl = s3Service.upload("productImage", productImage.getOriginalFilename(), productImage);
                File productFile = fileStorageService.saveFile(productImageUrl, FileType.product_image);

                // 기존 fileList의 두 번째 요소를 새로 업로드된 제품 이미지 파일로 대체
                fileList.set(1, productFile);
            }
            existingPost.updatePost(postRequestDto, parsedBuyDate, fileList);
            postRepository.save(existingPost);
        } catch (Exception e) {
            throw new PostException("Failed to update post", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Transactional
    public void deletePost(Long id) {
        Post post = postRepository.findById(id).orElseThrow(() -> new PostException("Post not found with id: " + id, HttpStatus.NOT_FOUND));
        try {
            if (post.getPostStatus() != PostStatus.거래가능) {
                throw new PostException("Cannot delete post with id: " + id + " because its status is not 'AVAILABLE'.", HttpStatus.BAD_REQUEST);
            }
            // S3에서 파일 삭제
            post.getFileList().forEach(file -> {
                s3Service.delete(file.getFileUrl());
            });
            // Post와 연관된 File 엔티티는 cascade = CascadeType.ALL로 인해 자동으로 삭제됨
            postRepository.delete(post);
        } catch (Exception e) {
            throw new PostException("Failed to delete post", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Transactional
    public void updatePostStatusToTrading(Long id) {
        Post post = postRepository.findById(id).orElseThrow(() -> new PostException("Post not found", HttpStatus.NOT_FOUND));
        post.setPostStatus(PostStatus.거래중);
        postRepository.save(post);
    }

    @Transactional
    public void updatePostStatusToCompleted(Long id) {
        Post post = postRepository.findById(id).orElseThrow(() -> new PostException("Post not found", HttpStatus.NOT_FOUND));
        post.setPostStatus(PostStatus.거래완료);
        postRepository.save(post);
    }

    private PostResponseDto convertToDto(Post post) {
        return PostResponseDto.builder()
                .postId(post.getId())
                .title(post.getTitle())
                .likeCount(post.getPostLikeList().size())
                .productImageUrl(post.getFileList().isEmpty() ? null : post.getFileList().get(1).getFileUrl())
                .count(post.getCount())
                .cost(post.getCost())
                .build();
    }
}
