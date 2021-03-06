package com.hanghae.naegahama.dto.answer;

import com.hanghae.naegahama.domain.Answer;
import com.hanghae.naegahama.util.TimeHandler;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@NoArgsConstructor
@Getter
public class AnswerDetailGetResponseDto
{
    private Long answerId;
    private Long requestWriterId;

    private Long answerWriterId;

    private String title;

    private String content;

    private String category;

    private String modifiedAt;

    private int star;

    private Long answerLikeCount;

    private Long commentCount;

    private String answerWriter;

    private List<Long> likeUserList;

    private List<String> fileList;

    private String videoUrl;
    
    private String imgUrl;
    private Long userId;

    public AnswerDetailGetResponseDto(Answer answer, Long likeCount, Long commentCount, List<Long> likeUserList,
                                      List<String> fileList, String category,String url) {
        this.answerId = answer.getId();
        this.requestWriterId = answer.getPost().getUser().getId();
        this.answerWriterId = answer.getUser().getId();
        this.title = answer.getTitle();
        this.content = answer.getContent();
        this.modifiedAt = TimeHandler.setModifiedAtAnswerDetail(answer.getModifiedAt());
        this.star = answer.getStar();
        this.answerLikeCount = likeCount;
        this.commentCount = commentCount;
        this.answerWriter = answer.getUser().getNickName();
        this.likeUserList = likeUserList;
        this.fileList = fileList;
        this.category = category;
        this.videoUrl = url;
        this.imgUrl = answer.getUser().getHippoImage();
        this.userId = answer.getUser().getId();
    }

    

}