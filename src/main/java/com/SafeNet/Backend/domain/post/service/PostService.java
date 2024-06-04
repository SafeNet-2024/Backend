package com.SafeNet.Backend.domain.post.service;

import com.SafeNet.Backend.domain.file.entity.File;
import com.SafeNet.Backend.domain.file.entity.FileType;
import com.SafeNet.Backend.domain.file.service.FileStorageService;
import com.SafeNet.Backend.domain.file.service.S3Service;
import com.SafeNet.Backend.domain.member.entity.Member;
import com.SafeNet.Backend.domain.post.dto.PostRequestDto;
import com.SafeNet.Backend.domain.post.dto.PostResponseDto;
import com.SafeNet.Backend.domain.post.entity.Post;
import com.SafeNet.Backend.domain.post.entity.PostStatus;
import com.SafeNet.Backend.domain.post.exception.PostException;
import com.SafeNet.Backend.domain.post.repository.PostRepository;
import com.SafeNet.Backend.domain.post.util.PostDtoConverter;
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

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class PostService {
    private final PostRepository postRepository;
    private final FileStorageService fileStorageService;
    private final CommonPostService commonPostService;
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy년 M월 d일");
    private final S3Service s3Service;

    @Transactional
    public void createPost(PostRequestDto postRequestDto, MultipartFile receiptImage, MultipartFile productImage, String email) {
        Member member = commonPostService.getMemberByEmail(email);
        try {
            LocalDate parsedBuyDate = LocalDate.parse(postRequestDto.getBuyDate(), formatter);
            Region region = member.getRegion(); // 로그인한 사용자의 지역 정보 추출
            String receiptImageUrl = s3Service.upload("receiptImage", receiptImage.getOriginalFilename(), receiptImage);
            String productImageUrl = s3Service.upload("productImage", productImage.getOriginalFilename(), productImage);
            File receiptFile = fileStorageService.saveFile(receiptImageUrl, FileType.receipt);
            File productFile = fileStorageService.saveFile(productImageUrl, FileType.product_image);
            Post post = Post.builder()
                    .title(postRequestDto.getTitle())
                    .category(postRequestDto.getCategory())
                    .cost(postRequestDto.getCost())
                    .count(postRequestDto.getCount())
                    .buyDate(parsedBuyDate)
                    .contents(postRequestDto.getContents())
                    .member(member)
                    .region(region) // 지역 정보 추가
                    .fileList(Arrays.asList(receiptFile, productFile))
                    .build();
            postRepository.save(post);
        } catch (Exception e) {
            throw new PostException("Failed to create post", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    public List<PostResponseDto> getAllPosts(String email) {
        return commonPostService.getAllPosts(email);
    }

    // 게시물 상세 조회
    public Optional<PostResponseDto> getPostById(Long id) {
        return postRepository.findById(id).map(PostService::convertToDetailDto);
    }

    private static PostResponseDto convertToDetailDto(Post post) {
        return PostResponseDto.builder()
                .postId(post.getId())
                .category(post.getCategory())
                .isLikedByCurrentUser(false)
                .productImageUrl(post.getFileList().isEmpty() ? null : post.getFileList().get(1).getFileUrl())
                .receiptImageUrl(post.getFileList().isEmpty() ? null : post.getFileList().get(0).getFileUrl())
                .title(post.getTitle())
                .count(post.getCount())
                .buyDate(post.getBuyDate() != null ? post.getBuyDate().toString() : null)
                .contents(post.getContents())
                .writer(post.getMember().getName())
                .cost(post.getCost())
                .build();
    }


    @Transactional
    public void updatePost(Long id, PostRequestDto postRequestDto, MultipartFile receiptImage, MultipartFile productImage, String email) {
        Post existingPost = postRepository.findById(id).orElseThrow(() -> new PostException("Post not found with id: " + id, HttpStatus.NOT_FOUND));
        if (!existingPost.getMember().getEmail().equals(email)) {
            throw new PostException("You do not have permission to update this post", HttpStatus.FORBIDDEN);
        }
        try {
            if (existingPost.getPostStatus() != PostStatus.거래가능) {
                throw new PostException("Cannot update post with id: " + id + " because its status is not 'AVAILABLE'.", HttpStatus.BAD_REQUEST);
            }
            LocalDate parsedBuyDate = LocalDate.parse(postRequestDto.getBuyDate(), formatter);
            List<File> fileList = existingPost.getFileList();
            if (receiptImage != null && !receiptImage.isEmpty()) {
                s3Service.delete(fileList.get(0).getFileUrl());
                fileStorageService.deleteFile(fileList.get(0));
                String receiptImageUrl = s3Service.upload("receiptImage", receiptImage.getOriginalFilename(), receiptImage);
                File receiptFile = fileStorageService.saveFile(receiptImageUrl, FileType.receipt);
                fileList.set(0, receiptFile);
            }
            if (productImage != null && !productImage.isEmpty()) {
                s3Service.delete(fileList.get(1).getFileUrl());
                fileStorageService.deleteFile(fileList.get(1));
                String productImageUrl = s3Service.upload("productImage", productImage.getOriginalFilename(), productImage);
                File productFile = fileStorageService.saveFile(productImageUrl, FileType.product_image);
                fileList.set(1, productFile);
            }
            existingPost.updatePost(postRequestDto, parsedBuyDate, fileList);
            postRepository.save(existingPost);
        } catch (Exception e) {
            throw new PostException("Failed to update post", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Transactional
    public void deletePost(Long id, String email) {
        Post post = postRepository.findById(id).orElseThrow(() -> new PostException("Post not found with id: " + id, HttpStatus.NOT_FOUND));
        if (!post.getMember().getEmail().equals(email)) {
            throw new PostException("You do not have permission to delete this post", HttpStatus.FORBIDDEN);
        }
        try {
            if (post.getPostStatus() != PostStatus.거래가능) {
                throw new PostException("Cannot delete post with id: " + id + " because its status is not 'AVAILABLE'.", HttpStatus.BAD_REQUEST);
            }
            post.getFileList().forEach(file -> s3Service.delete(file.getFileUrl()));
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
}
