package com.SafeNet.Backend.domain.post.repository;

import com.SafeNet.Backend.domain.post.entity.Category;
import com.SafeNet.Backend.domain.post.entity.Post;
import io.lettuce.core.dynamic.annotation.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PostRepository extends JpaRepository<Post, Long> {
    List<Post> findByMember_Id(Long memberId);

    List<Post> findByRegion_IdAndCategory(Long memberRegionId, Category category);

    List<Post> findByRegion_IdOrderByCreatedDesc(Long memberRegionId);

    List<Post> findByRegion_IdOrderByBuyDateDesc(Long memberRegionId);

    List<Post> findByRegion_Id(Long regionId);
    @Query("SELECT p FROM Post p WHERE p.region.id = :regionId AND (p.title LIKE %:keyword% OR p.contents LIKE %:keyword%)")
    List<Post> findByRegionIdAndTitleOrContentsContaining(@Param("regionId") Long regionId, @Param("keyword") String keyword);
}

