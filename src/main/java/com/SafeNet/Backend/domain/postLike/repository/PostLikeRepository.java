package com.SafeNet.Backend.domain.postLike.repository;

import com.SafeNet.Backend.domain.postLike.domain.PostLike;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PostLikeRepository extends JpaRepository<PostLike, Long> {
}