package com.SafeNet.Backend.domain.post.service;

import com.SafeNet.Backend.domain.post.dto.PostResponseDto;
import com.SafeNet.Backend.domain.post.entity.Category;
import com.SafeNet.Backend.domain.post.entity.Post;
import com.SafeNet.Backend.domain.post.exception.PostException;
import com.SafeNet.Backend.domain.post.repository.PostRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class PostSearchAndSortService {
    private final PostRepository postRepository;

    public List<PostResponseDto> searchByKeyWord(String keyword) { // 글 제목 또는 설명으로 검색
        try {
            List<Post> posts = postRepository.findByTitleContainingOrContentsContaining(keyword, keyword);
            return posts.stream().map(this::convertToDto).collect(Collectors.toList());
        } catch (Exception e) {
            throw new PostException("Failed to retrieve posts by KeyWord", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    public List<PostResponseDto> searchByCategory(Category category) { // 카테고리로 검색
        try {
            List<Post> posts = postRepository.findByCategory(category);
            return posts.stream().map(this::convertToDto).collect(Collectors.toList());
        } catch (Exception e) {
            throw new PostException("Failed to retrieve posts by Category", HttpStatus.INTERNAL_SERVER_ERROR);
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
