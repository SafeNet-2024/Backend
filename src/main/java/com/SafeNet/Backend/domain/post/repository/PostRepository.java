package com.SafeNet.Backend.domain.post.repository;

import com.SafeNet.Backend.domain.post.entity.Category;
import com.SafeNet.Backend.domain.post.entity.Post;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PostRepository extends JpaRepository<Post, Long> {
    List<Post> findByMember_Id(Long memberId);

    List<Post> findByRegion_IdAndCategory(Long memberRegionId, Category category);

    List<Post> findByRegion_IdOrderByCreatedDesc(Long memberRegionId);

    List<Post> findByRegion_IdOrderByBuyDateDesc(Long memberRegionId);

    List<Post> findByRegion_Id(Long regionId);
    List<Post> findByRegion_IdAndTitleContainingOrContentsContaining(Long regionId, String title, String contents);

}

