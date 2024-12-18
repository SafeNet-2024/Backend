package com.SafeNet.Backend.domain.post.dto;

import com.SafeNet.Backend.domain.post.entity.Category;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;


@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PostRequestDto {
    private Category category;
    private String title;
    private int cost;
    private int count;
    private String buyDate; // LocalDate 대신 String으로 받음
    private String contents;
}
