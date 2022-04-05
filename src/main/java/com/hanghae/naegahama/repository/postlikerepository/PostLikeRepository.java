package com.hanghae.naegahama.repository.postlikerepository;

import com.hanghae.naegahama.domain.PostLike;
import com.hanghae.naegahama.domain.Post;
import com.hanghae.naegahama.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PostLikeRepository extends JpaRepository<PostLike, Long> {
    //Optional<PostLike> findByUserAndPost(User user, Post post);
  //  Long countByPost(Post post);

}
