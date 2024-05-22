package com.SafeNet.Backend.domain.post.service;

import com.SafeNet.Backend.domain.file.entity.File;
import com.SafeNet.Backend.domain.file.entity.FileType;
import com.SafeNet.Backend.domain.file.service.FileStorageService;
import com.SafeNet.Backend.domain.member.entity.Member;
import com.SafeNet.Backend.domain.member.repository.MemberRepository;
import com.SafeNet.Backend.domain.post.entity.Post;
import com.SafeNet.Backend.domain.post.entity.PostStatus;
import com.SafeNet.Backend.domain.post.dto.PostRequestDto;
import com.SafeNet.Backend.domain.post.dto.PostResponseDto;
import com.SafeNet.Backend.domain.post.exception.PostException;
import com.SafeNet.Backend.domain.post.repository.PostRepository;
import com.SafeNet.Backend.domain.postLike.entity.PostLike;
import com.SafeNet.Backend.domain.postLike.repository.PostLikeRepository;
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
    private final PostLikeRepository postLikeRepository;
    private final FileStorageService fileStorageService;
    private final MemberRepository memberRepository;
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy년 M월 d일");

    @Transactional
    public void createPost(PostRequestDto postRequestDto, MultipartFile receiptImage, MultipartFile productImage, Long memberId) {
        try {
            // 문자열로 받은 날짜 데이터를 LocalDate로 파싱
            LocalDate parsedBuyDate = LocalDate.parse(postRequestDto.getBuyDate(), formatter);

            // 사용자 정보로 Member 객체 조회
            Member member = memberRepository.findById(memberId).orElseThrow();
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
                        .imageUrl(post.getFileList().isEmpty() ? null : post.getFileList().get(1).getFileUrl())
                        .cost(post.getCost())
                        .count(post.getCount())
                        .buyDate(post.getBuyDate().toString())
                        .category(post.getCategory()).build())
                .orElseThrow(() -> new PostException("Post not found with id: " + id, HttpStatus.NOT_FOUND)));
    }

    @Transactional
    public void updatePost(Long id, PostRequestDto postRequestDto, MultipartFile receiptImage, MultipartFile productImage) {
        try {
            Post existingPost = postRepository.findById(id).orElseThrow(() -> new PostException("Post not found with id: " + id, HttpStatus.NOT_FOUND));

            if (existingPost.getPostStatus() != PostStatus.거래가능) {
                throw new PostException("Cannot update post with id: " + id + " because its status is not 'AVAILABLE'.", HttpStatus.BAD_REQUEST);
            }

            LocalDate parsedBuyDate = LocalDate.parse(postRequestDto.getBuyDate(), formatter);
            List<File> fileList = existingPost.getFileList();

            if (receiptImage != null && !receiptImage.isEmpty()) {
                fileStorageService.deleteFile(fileList.get(0));
                File receiptFile = fileStorageService.saveFile(receiptImage, FileType.receipt);
                fileList.set(0, receiptFile);
            }

            if (productImage != null && !productImage.isEmpty()) {
                fileStorageService.deleteFile(fileList.get(1));
                File productFile = fileStorageService.saveFile(productImage, FileType.product_image);
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
        try {
            Post post = postRepository.findById(id).orElseThrow(() -> new PostException("Post not found with id: " + id, HttpStatus.NOT_FOUND));

            if (post.getPostStatus() != PostStatus.거래가능) {
                throw new PostException("Cannot delete post with id: " + id + " because its status is not 'AVAILABLE'.", HttpStatus.BAD_REQUEST);
            }

            post.getFileList().forEach(fileStorageService::deleteFile);
            postRepository.delete(post);
        } catch (Exception e) {
            throw new PostException("Failed to delete post", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    public List<PostResponseDto> getPostsByMemberId(Long memberId) {
        try {
            List<Post> posts = postRepository.findByMember_Id(memberId);
            return posts.stream().map(this::convertToDto).collect(Collectors.toList());
        } catch (Exception e) {
            throw new PostException("Failed to retrieve posts by memberId", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    public List<PostResponseDto> getLikedPostsByMemberId(Long memberId) {
        try {
            List<PostLike> postLikes = postLikeRepository.findByMember_Id(memberId);
            return postLikes.stream().map(postLike -> this.convertToDto(postLike.getPost())).collect(Collectors.toList());
        } catch (Exception e) {
            throw new PostException("Failed to retrieve liked posts by memberId", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    private PostResponseDto convertToDto(Post post) {
        return PostResponseDto.builder()
                .postId(post.getId())
                .title(post.getTitle())
                .likeCount(post.getPostLikeList().size())
                .imageUrl(post.getFileList().isEmpty() ? null : post.getFileList().get(1).getFileUrl())
                .count(post.getCount())
                .cost(post.getCost())
                .build();
    }
}
