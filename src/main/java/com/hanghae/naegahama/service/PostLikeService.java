package com.hanghae.naegahama.service;

import com.hanghae.naegahama.domain.Alarm;
import com.hanghae.naegahama.domain.Post;
import com.hanghae.naegahama.domain.PostLike;
import com.hanghae.naegahama.domain.User;
import com.hanghae.naegahama.dto.alarm.MessageDto;
import com.hanghae.naegahama.dto.alarm.Type;
import com.hanghae.naegahama.dto.postlike.PostLikeRequestDto;
import com.hanghae.naegahama.dto.postlike.PostLikeResponseDto;
import com.hanghae.naegahama.repository.AlarmRepository;
import com.hanghae.naegahama.repository.PostLikeRepository;
import com.hanghae.naegahama.repository.PostRepository;
import com.hanghae.naegahama.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;

@RequiredArgsConstructor
@Service
@Slf4j
public class PostLikeService {
    private final PostLikeRepository postLikeRepository;
    private final UserRepository userRepository;
    private final PostRepository postRepository;
    private final AlarmRepository alarmRepository;
    private final AlarmService alarmService;

    @Transactional
    public PostLikeResponseDto PostLike(Long postId, Long id) {
        User user = userRepository.findById(id).orElseThrow(
                () -> new IllegalArgumentException("유저 정보가 없습니다.")
        );

        Post post = postRepository.findById(postId).orElseThrow(
                ()->new IllegalArgumentException("게시글이 없습니다.")
        );

        User postWriter = post.getUser();

        PostLike findPostLike = postLikeRepository.findByUserAndPost(user,post).orElse(null);
        log.info("userId ={}", user.getId());
        if(findPostLike == null){
            PostLikeRequestDto requestDto = new PostLikeRequestDto(user, post);
            PostLike postLike = new PostLike(requestDto);
            postLikeRepository.save(postLike);
            postWriter.addPoint(5);
        } else
        {
            postLikeRepository.deleteById(findPostLike.getId());
            postWriter.addPoint(-5);
        }
        if (!user.equals(findPostLike.getUser())) {
            Alarm alarm = new Alarm(user, findPostLike.getUser().getNickName(), Type.likeP, findPostLike.getPost().getId(), findPostLike.getPost().getTitle());
            Alarm save1 = alarmRepository.save(alarm);
            alarmService.alarmByMessage(new MessageDto(save1));
        }
        return new PostLikeResponseDto(postId, postLikeRepository.countByPost(post));
    }

}
