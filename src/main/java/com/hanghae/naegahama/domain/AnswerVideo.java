package com.hanghae.naegahama.domain;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Entity
@Getter
@NoArgsConstructor
public class AnswerVideo extends Timestamped{

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "file_id")
    private Long id;

    @Column(nullable = false, length = 1000)
    private String url;

    @JsonManagedReference
    @JoinColumn(name = "answer_id")
    @OneToOne(fetch = FetchType.LAZY)
    private Answer answer;

    public void setAnswer(Answer answer) {
        this.answer = answer;
    }

    public void setUrl(String url) {
        this.url = url;
    }
    public AnswerVideo(String url, Answer answer)
    {
        this.answer = answer;
        this.url = url;
    }
}
