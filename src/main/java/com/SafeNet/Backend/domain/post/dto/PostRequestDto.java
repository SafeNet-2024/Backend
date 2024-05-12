package com.SafeNet.Backend.domain.post.dto;

import com.SafeNet.Backend.domain.post.domain.Category;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Getter;
import org.springframework.web.multipart.MultipartFile;


@Getter
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PostRequestDto {
    private Category category;
    private String title;
    private Integer cost;
    private Integer count;
    private String buyDate; // LocalDate 대신 String으로 받음
    private String contents;
}
