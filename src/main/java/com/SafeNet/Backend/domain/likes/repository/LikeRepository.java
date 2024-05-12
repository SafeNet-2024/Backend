package com.SafeNet.Backend.domain.likes.repository;

import com.SafeNet.Backend.domain.likes.domain.Like;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LikeRepository extends JpaRepository<Like, Long> {
}
