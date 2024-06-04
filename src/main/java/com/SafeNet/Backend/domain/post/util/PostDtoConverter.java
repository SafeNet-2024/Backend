package com.SafeNet.Backend.domain.post.util;
import com.SafeNet.Backend.domain.post.dto.PostResponseDto;
import com.SafeNet.Backend.domain.post.entity.Post;

public class PostDtoConverter {

    public static PostResponseDto convertToDto(Post post, boolean isLikedByCurrentUser) {
        return PostResponseDto.builder()
                .postId(post.getId())
                .isLikedByCurrentUser(isLikedByCurrentUser)
                .productImageUrl(post.getFileList().isEmpty() ? null : post.getFileList().get(1).getFileUrl())
                .title(post.getTitle())
                .build();
    }
}

