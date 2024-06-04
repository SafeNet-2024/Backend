package com.SafeNet.Backend.domain.post.service;

import com.SafeNet.Backend.domain.post.dto.PostResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class MemberItemService {
    private final CommonPostService commonPostService;

    public List<PostResponseDto> getPostsByMemberId(String email) {
        return commonPostService.getPostsByMemberId(email);
    }

    public List<PostResponseDto> getLikedPostsByMemberId(String email) {
        return commonPostService.getLikedPostsByMemberId(email);
    }
}
