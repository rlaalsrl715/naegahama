package com.hanghae.naegahama.dto.file;

import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
public class FileResponseDto
{
    private List<String> file;
    private String video;

    public FileResponseDto(List<String> file, String video )
    {
        this.file = file;
        this.video = video;

    }
}
