package com.SafeNet.Backend.domain.post.util;
import com.SafeNet.Backend.domain.post.dto.PostResponseDto;
import com.SafeNet.Backend.domain.post.entity.Post;

public class PostDtoConverter {

    public static PostResponseDto convertToDto(Post post, boolean isLikedByCurrentUser) {
        return PostResponseDto.builder()
                .postId(post.getId())
                .category(post.getCategory())
                .likeCount(isLikedByCurrentUser)
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
}

