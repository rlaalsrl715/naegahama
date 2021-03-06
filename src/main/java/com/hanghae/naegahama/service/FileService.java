package com.hanghae.naegahama.service;

import com.hanghae.naegahama.domain.AnswerFile;
import com.hanghae.naegahama.domain.PostFile;
import com.hanghae.naegahama.dto.file.FileResponseDto;
import com.hanghae.naegahama.dto.file.ImageUrlResponseDto;
import com.hanghae.naegahama.repository.answerrepository.AnswerRepository;
import com.hanghae.naegahama.repository.postrepository.PostRepository;
import com.hanghae.naegahama.util.ComfortMethods;
import com.hanghae.naegahama.util.S3Uploader;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@RequiredArgsConstructor
@Service
public class FileService
{
    private final S3Uploader s3Uploader;


    public FileResponseDto fileURL(List<MultipartFile> multipartFileList, MultipartFile videoFile) throws IOException
    {
        // 파일 혹은 비디오 파일을 넣지 않았을 경우 공백으로 처리.
        List<String> file = new ArrayList<>();
        String video = "";

        // 파일이 있을 경우 s3에 넣고 url 값을 받음.
        if ( multipartFileList != null)
        {
            for ( MultipartFile multipartFile : multipartFileList)
            {
                String fileUrl = s3Uploader.upload(multipartFile, "static",false);
                file.add(fileUrl);
            }
        }

        // 비디오가 있을 경우 s3에 넣고 url 값을 받음.
        if ( videoFile != null)
        {
            video = s3Uploader.upload(videoFile, "static",true);
        }

        FileResponseDto fileResponseDto = new FileResponseDto(file, video);

        return fileResponseDto;

    }


    public ImageUrlResponseDto imgUrlList(String type, Long id)
    {
        List<String> imageUrl = new ArrayList<>();

        if(type.equals("post"))
        {
            List<PostFile> fileList = ComfortMethods.getPost(id).getFileList();

            for ( PostFile file : fileList)
            {
                imageUrl.add(file.getUrl());
            }
        }
        else if ( type.equals("answer"))
        {
            List<AnswerFile> fileList = ComfortMethods.getAnswer(id).getFileList();
            for ( AnswerFile file : fileList)
            {
                imageUrl.add(file.getUrl());
            }
        }
        else
        {
            throw new IllegalArgumentException("잘못된 타입입니다.");
        }

        return new ImageUrlResponseDto(imageUrl);

    }
}
