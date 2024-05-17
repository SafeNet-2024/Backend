package com.SafeNet.Backend.domain.post.repository;

import com.SafeNet.Backend.domain.post.domain.Post;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PostRepository extends JpaRepository<Post, Long> {
    List<Post> findByMember_Id(Long memberId);
}

