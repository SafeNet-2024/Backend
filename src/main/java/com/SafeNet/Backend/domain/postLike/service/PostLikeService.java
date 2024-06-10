    package com.SafeNet.Backend.domain.postLike.service;

    import com.SafeNet.Backend.domain.member.entity.Member;
    import com.SafeNet.Backend.domain.member.repository.MemberRepository;
    import com.SafeNet.Backend.domain.member.service.MemberService;
    import com.SafeNet.Backend.domain.post.entity.Post;
    import com.SafeNet.Backend.domain.post.exception.PostException;
    import com.SafeNet.Backend.domain.post.repository.PostRepository;
    import com.SafeNet.Backend.domain.postLike.entity.PostLike;
    import com.SafeNet.Backend.domain.postLike.repository.PostLikeRepository;
    import jakarta.transaction.Transactional;
    import lombok.RequiredArgsConstructor;
    import org.springframework.http.HttpStatus;
    import org.springframework.stereotype.Service;

    import java.util.Optional;

    @Service
    @RequiredArgsConstructor
    public class PostLikeService {
        private final PostLikeRepository postLikeRepository;

        private final PostRepository postRepository;
        private final MemberRepository memberRepository;

        @Transactional
        public boolean likePost(Long postId, String email) {
            Post post = postRepository.findById(postId).orElseThrow(() -> new PostException("Post not found with id: " + postId, HttpStatus.NOT_FOUND)); // 유효한 게시물인지 검사
            Member member = memberRepository.findByEmail(email).orElseThrow(() -> new PostException("Member not found with email: " + email, HttpStatus.NOT_FOUND)); // 유효한 멤버인지 검사

            if (post.getMember().getEmail().equals(email)) {
                throw new PostException("You cannot like your own post", HttpStatus.BAD_REQUEST); // 자신의 게시물일 경우 좋아요를 생성하지 못하게 하기
            }

            Optional<PostLike> existingLike = postLikeRepository.findByPostIdAndMemberId(postId, member.getId());

            if (existingLike.isPresent()) {
                postLikeRepository.delete(existingLike.get());
                return false; // 좋아요 취소
            } else {
                PostLike postLike = PostLike.builder()
                        .post(post)
                        .member(member)
                        .build();
                postLikeRepository.save(postLike);
                return true; // 좋아요 추가
            }
        }
    }
