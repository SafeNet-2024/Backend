package com.SafeNet.Backend.domain.post.api;

import com.SafeNet.Backend.domain.post.dto.PostResponseDto;
import com.SafeNet.Backend.domain.post.entity.Category;
import com.SafeNet.Backend.domain.post.service.PostSearchAndSortService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/posts")
@RequiredArgsConstructor
@Tag(name = "Post Query", description = "Post Query API")
public class PostSearchAndSortController {
    private final PostSearchAndSortService postSearchAndSortService;

    @GetMapping("/keyword")
    public ResponseEntity<List<PostResponseDto>> searchByKeyWord(@RequestParam("keyword") String keyword) {
        List<PostResponseDto> posts = postSearchAndSortService.searchByKeyWord(keyword);
        return ResponseEntity.ok(posts);
    }

    @GetMapping("/category")
    public ResponseEntity<List<PostResponseDto>> searchByCategory(@RequestParam("category") Category category) {
        List<PostResponseDto> posts = postSearchAndSortService.searchByCategory(category);
        return ResponseEntity.ok(posts);
    }

    @GetMapping("/sort/created")
    public ResponseEntity<List<PostResponseDto>> sortByCreated() {
        List<PostResponseDto> posts = postSearchAndSortService.sortByCreated();
        return ResponseEntity.ok(posts);
    }

    @GetMapping("/sort/buyDate")
    public ResponseEntity<List<PostResponseDto>> sortByBuyDate() {
        List<PostResponseDto> posts = postSearchAndSortService.sortByBuyDate();
        return ResponseEntity.ok(posts);
    }
}

