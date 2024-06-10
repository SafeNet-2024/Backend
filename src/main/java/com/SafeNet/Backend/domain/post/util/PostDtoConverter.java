package com.SafeNet.Backend.domain.post.util;
import com.SafeNet.Backend.domain.post.dto.PostResponseDto;
import com.SafeNet.Backend.domain.post.entity.Post;
import com.SafeNet.Backend.domain.post.entity.PostStatus;

public class PostDtoConverter {

    public static PostResponseDto convertToDto(Post post, boolean isLikedByCurrentUser, boolean isMine, PostStatus postStatus) {
        return PostResponseDto.builder()
                .postId(post.getId())
                .isLikedByCurrentUser(isLikedByCurrentUser)
                .productImageUrl(post.getFileList().isEmpty() ? null : post.getFileList().get(1).getFileUrl())
                .title(post.getTitle())
                .isMine(isMine)
                .postStatus(postStatus)
                .build();
    }
}

