package com.hanghae.naegahama.repository.userpagecommentrepository;

import com.hanghae.naegahama.domain.UserComment;
import org.springframework.data.jpa.repository.JpaRepository;



public interface UserPageCommentRepository extends JpaRepository<UserComment, Long> {

   // List<UserComment> findByPageUserOrderByModifiedAt(User user);

//    List<UserComment> findByParentCommentIdOrderByModifiedAt(Long parentCommentId);
}
