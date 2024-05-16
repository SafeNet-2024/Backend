package com.SafeNet.Backend.domain.post.dto;

import com.SafeNet.Backend.domain.post.domain.Category;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PostResponseDto {
    private Long postId;
    private Category category;
    private int likeCount;
    private String imageUrl;
    private String title;
    private int count;
    private String buyDate; // LocalDate 대신 String으로 받음
    private String contents;
    private String writer;
    private int price;
}

