package com.SafeNet.Backend.domain.post.service;

import com.SafeNet.Backend.domain.post.dto.PostResponseDto;
import com.SafeNet.Backend.domain.post.entity.Category;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class PostSearchAndSortService {
    private final CommonPostService commonPostService;

    public List<PostResponseDto> searchByKeyWord(String keyword, String email) {
        return commonPostService.searchPostsByKeyword(keyword, email);
    }

    public List<PostResponseDto> searchByCategory(Category category, String email) {
        return commonPostService.searchPostsByCategory(category, email);
    }

    public List<PostResponseDto> sortByCreated(String email) {
        return commonPostService.sortPostsByCreated(email);
    }

    public List<PostResponseDto> sortByBuyDate(String email) {
        return commonPostService.sortPostsByBuyDate(email);
    }
}
