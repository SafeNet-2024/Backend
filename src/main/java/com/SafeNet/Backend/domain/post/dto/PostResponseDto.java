package com.SafeNet.Backend.domain.post.dto;

import com.SafeNet.Backend.domain.post.entity.Category;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PostResponseDto {
    private Long postId;
    private Category category;
    private boolean  likeCount; // 현재 사용자가 하트를 눌렀는지 여부
    private String productImageUrl; // 상품 이미지 URL
    private String receiptImageUrl; // 영수증 이미지 URL
    private String title;
    private int count;
    private String buyDate; // LocalDate 대신 String으로 받음
    private String contents;
    private String writer;
    private int cost;
}

