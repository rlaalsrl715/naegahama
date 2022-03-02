package com.hanghae.naegahama.repository;

import com.hanghae.naegahama.domain.Answer;
import com.hanghae.naegahama.domain.Comment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CommentRepository extends JpaRepository<Comment, Long> {

    Long countByAnswer(Answer answer);

    List<Comment> findAllByAnswer(Answer answer);

    List<Comment> findAllByParentCommentId(Long id);

    void deleteByAnswer(Answer answer);
}
