package com.hanghae.naegahama.dto.post;

import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotBlank;
import java.util.List;

@Setter
@Getter
public class PutRequestDto
{
    @NotBlank(message = "내용은 필수입니다.")
    private String content;
    private List<String> file;
}
