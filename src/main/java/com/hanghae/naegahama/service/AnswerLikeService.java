package com.hanghae.naegahama.service;

import com.hanghae.naegahama.domain.*;
import com.hanghae.naegahama.dto.answerlike.AnswerLikeRequestDto;
import com.hanghae.naegahama.dto.answerlike.AnswerLikeResponseDto;
import com.hanghae.naegahama.handler.event.AnswerLikeEvent;
import com.hanghae.naegahama.repository.*;
import com.hanghae.naegahama.security.UserDetailsImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;

@RequiredArgsConstructor
@Service
public class AnswerLikeService {
    private final AnswerLikeRepository answerLikeRepository;
    private final AnswerRepository answerRepository;
    private final ApplicationEventPublisher applicationEventPublisher;


    @Transactional
    public AnswerLikeResponseDto AnswerLike(Long answerId, UserDetailsImpl userDetails) {
        User user = userDetails.getUser();

        Answer answer = answerRepository.findById(answerId).orElseThrow(
                ()->new IllegalArgumentException("답변글이 없습니다.")
        );

        User answerWriter = answer.getUser();

        //오 좋은 거 배우고 갑니다.
        AnswerLike findAnswerLike = answerLikeRepository.findByUserAndAnswer(user,answer).orElse(null);

        if(findAnswerLike == null){
            AnswerLike answerLike = new AnswerLike(new AnswerLikeRequestDto(user, answer));
            answerLikeRepository.save(answerLike);
            applicationEventPublisher.publishEvent(new AnswerLikeEvent(answerWriter,user,answer));
        } else {
            answerLikeRepository.deleteById(findAnswerLike.getId());
            if(!answerWriter.getNickName().equals(user.getNickName())) {
                answerWriter.setPoint(answerWriter.getPoint() - 25);
            }
        }

        return new AnswerLikeResponseDto(answerId, answerLikeRepository.countByAnswer(answer));
    }
}
