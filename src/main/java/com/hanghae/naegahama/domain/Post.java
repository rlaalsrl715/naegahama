package com.hanghae.naegahama.domain;

import com.hanghae.naegahama.config.auth.UserDetailsImpl;
import com.hanghae.naegahama.dto.post.PostRequestDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.Length;

import javax.persistence.*;
import javax.validation.constraints.NotEmpty;
import java.util.List;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
public class Post extends Timestamped{
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "post_id", nullable = false)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Length(max = 10000)
    @Column(nullable = false)
    private String content;

    @Column(nullable = false)
    private String category;

    @Column(nullable = false)
    private Integer level;

    @JoinColumn(name = "user_id")
    @ManyToOne
    private User user;

    @OneToMany(mappedBy = "answer")
    private List<Answer> answerList;

    @Builder
    public Post(PostRequestDto postRequestDto, UserDetailsImpl userDetails) {
        this.user = userDetails.getUser();
        this.title = postRequestDto.getTitle();
        this.content = postRequestDto.getContent();
        this.content = postRequestDto.getCategory();
        this.level = Integer.valueOf(postRequestDto.getLevel());
    }

    public void updatePost(PostRequestDto postRequestDto) {
        this.title = postRequestDto.getTitle();
        this.content = postRequestDto.getContent();
        this.content = postRequestDto.getCategory();
        this.level = Integer.valueOf(postRequestDto.getLevel());
    }
}
