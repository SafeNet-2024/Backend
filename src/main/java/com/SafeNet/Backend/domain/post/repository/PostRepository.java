package com.SafeNet.Backend.domain.post.repository;

import com.SafeNet.Backend.domain.post.entity.Category;
import com.SafeNet.Backend.domain.post.entity.Post;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PostRepository extends JpaRepository<Post, Long> {
    List<Post> findByMember_Id(Long memberId);

    // 글 제목 또는 설명으로 검색
    List<Post> findByTitleContainingOrContentsContaining(String title, String contents);

    // 카테고리로 검색
    List<Post> findByCategory(Category category);
}

