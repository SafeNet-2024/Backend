package com.SafeNet.Backend.domain.postLike.repository;

import com.SafeNet.Backend.domain.postLike.entity.PostLike;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PostLikeRepository extends JpaRepository<PostLike, Long> {
    List<PostLike> findByMember_Id(Long memberId);
    Optional<PostLike> findByPostIdAndMemberId(Long postId, Long memberId);
    boolean existsByPostIdAndMemberId(Long postId, Long memberId); // 좋아요가 눌려있는지 확인
}
