package com.hanghae.naegahama.service;

import com.hanghae.naegahama.config.auth.UserDetailsImpl;
import com.hanghae.naegahama.domain.*;
import com.hanghae.naegahama.dto.BasicResponseDto;
import com.hanghae.naegahama.dto.MyPage.MyPostDto;
import com.hanghae.naegahama.dto.category.CategoryResponseDto;
import com.hanghae.naegahama.dto.post.*;
import com.hanghae.naegahama.handler.ex.PostNotFoundException;
import com.hanghae.naegahama.repository.*;

import com.hanghae.naegahama.util.S3Uploader;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.transaction.Transactional;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@EnableAutoConfiguration
@RequiredArgsConstructor
@Service
@Slf4j
@Transactional
public class PostService {

    private final PostRepository postRepository;
    private final CommentRepository commentRepository;
    private final AnswerRepository answerRepository;
    private final PostLikeRepository postLikeRepository;

    private final S3Uploader s3Uploader;

    private final PostFileRepository postFileRepository;

    private final UserRepository userRepository;

    private final String publishing = "작성완료";
    private final String temporary = "임시저장";

    //요청글 작성
    @Transactional
    public ResponseEntity<?> createPost(PostRequestDto postRequestDto, User user) throws IOException
    {
        if (postRequestDto.getTitle() == null)
        {
            throw new IllegalArgumentException("제목을 입력해주세요.");
        }

        String content = postRequestDto.getContent();
        if (postRequestDto.getContent() == null)
        {
            throw new IllegalArgumentException("내용을 입력해주세요.");
        }

        if (content.length() > 1000)
        {
            throw new IllegalArgumentException("1000자 이하로 입력해주세요.");
        }

        // 전달받은 정보로 post 객체 생성
        Post post = new Post(postRequestDto, user, publishing);

        //객체 저장 및 저장된 post 꺼내옴
        Post savePost = postRepository.save(post);

        // 이미지 파일 url 배열로 for 반복문을 실행
        for ( String url : postRequestDto.getFile())
        {
            // 이미지 파일 url로 postFile 객체 생성
            PostFile fileUrl = new PostFile(url);

            // postFile에 savePost를 연관관계 설정
            fileUrl.setPost(savePost);

            // 이미지 파일 url 1개에 해당되는 postFile을 DB에 저장
            PostFile saveFile = postFileRepository.save(fileUrl);

            // 저장된 postFile을 저장된 post에 한개씩 추가함
            savePost.getFileList().add(saveFile);
        }

        // 잔여시간 처리
        LocalDateTime deadline = savePost.getCreatedAt().plusHours(postRequestDto.getTimeSet());

        String time = deadline.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH-mm"));
        savePost.setTimeSet(time);

        // 임시 작성중이던 모든 글 삭제
        List<Post> deleteList = postRepository.findAllByUserAndState(user, temporary);

        for ( Post deletePost : deleteList)
        {
            postFileRepository.deleteByPost(deletePost);
            postRepository.deleteById(deletePost.getId());
        }


        // 퍼블리싱 이벤트? 그거 써서 처리해야함.
        // 최초 요청글 작성시 업적 5 획득
//        User achievementUser = userRepository.findById(user.getId()).orElseThrow(
//                () -> new IllegalArgumentException("업적 달성 유저가 존재하지 않습니다."));
//        achievementUser.getAchievement().setAchievement5(1);

        return ResponseEntity.ok().body(new BasicResponseDto("true"));

    }

    // 요청글 임시 저장
    public ResponseEntity<?> temporaryPost(PostRequestDto postRequestDto, User user)
    {
        // 전달받은 정보로 post 객체 생성
        Post post = new Post(postRequestDto, user, temporary);

        //객체 저장 및 저장된 post 꺼내옴
        Post savePost = postRepository.save(post);

        // 이미지 파일 url 배열로 for 반복문을 실행
        for ( String url : postRequestDto.getFile())
        {
            // 이미지 파일 url로 postFile 객체 생성
            PostFile fileUrl = new PostFile(url);

            // postFile에 savePost를 연관관계 설정
            fileUrl.setPost(savePost);

            // 이미지 파일 url 1개에 해당되는 postFile을 DB에 저장
            PostFile saveFile = postFileRepository.save(fileUrl);

            // 저장된 postFile을 저장된 post에 한개씩 추가함
            savePost.getFileList().add(saveFile);

        }

        return ResponseEntity.ok().body(new BasicResponseDto("true"));
    }

    // 요청글 불러오기
    public ResponseEntity<?> temporaryLoad(UserDetailsImpl userDetails)
    {
        List<GetTemporaryResponseDto> getTemporaryResponseDtoList = new ArrayList<>();

        User user = userDetails.getUser();

        List<Post> postList = postRepository.findAllByUserAndState(user, temporary);

        for ( Post post : postList)
        {
            GetTemporaryResponseDto getTemporaryResponseDto = new GetTemporaryResponseDto(post);
            getTemporaryResponseDtoList.add(getTemporaryResponseDto);
        }

        return ResponseEntity.ok().body(getTemporaryResponseDtoList);
    }


    //요청글 수정
    @Transactional
    public ResponseEntity<?> updatePost(Long id, PutRequestDto postRequestDto, UserDetailsImpl userDetails)
    {
        Post post = postRepository.findById(id).orElseThrow(
                () -> new IllegalArgumentException("게시글이 존재하지 않습니다.")
        );


        User user = post.getUser();

        System.out.println(userDetails.getUser().getId());
        System.out.println(user.getId());

        if (!userDetails.getUser().getId().equals(user.getId()))
        {
            throw new IllegalArgumentException("작성자만 수정할 수 있습니다.");
        }

        if (postRequestDto.getContent() == null) {
            throw new IllegalArgumentException("내용을 입력해주세요.");
        }
        if (postRequestDto.getContent().length() > 1000) {
            throw new IllegalArgumentException("1000자 이하로 입력해주세요.");
        }
        post.UpdatePost(postRequestDto);

        // 기존에 있던 이미지 파일 S3에서 삭제
        for ( PostFile deleteS3 : post.getFileList())
        {
            String[] fileKey = deleteS3.getUrl().split("static/");
            try
            {
                String decodeKey = URLDecoder.decode(fileKey[1], "UTF-8");
                s3Uploader.deleteS3("static/" + decodeKey);
            }
            catch (UnsupportedEncodingException e)
            {
                e.printStackTrace();
            }
        }
        // 기존에 있던 포스트파일 제거
        postFileRepository.deleteByPost(post);

        // 새로운 이미지 파일 url 배열로 for 반복문을 실행
        for ( String url : postRequestDto.getFile())
        {
            // 이미지 파일 url로 postFile 객체 생성
            PostFile fileUrl = new PostFile(url);

            // postFile에 savePost를 연관관계 설정
            fileUrl.setPost(post);

            // 이미지 파일 url 1개에 해당되는 postFile을 DB에 저장
            PostFile saveFile = postFileRepository.save(fileUrl);

            // 저장된 postFile을 저장된 post에 한개씩 추가함
            post.getFileList().add(saveFile);
        }

        return ResponseEntity.ok().body(new BasicResponseDto("true"));
    }

//    //요청글 삭제
//    @Transactional
//    public Post deletePost(Long id, UserDetailsImpl userDetails) {
//        Post post = postRepository.findById(id).orElseThrow(
//                () -> new IllegalArgumentException("게시글이 존재하지 않습니다.")
//        );
//        User user = post.getUser();
//        Long deleteId = user.getId();
//        if (userDetails.getUser().getId() != deleteId) {
//            throw new IllegalArgumentException("작성자만 수정할 수 있습니다.");
//        }
//
//        List<Comment> comments = commentRepository.findAllByAnswer(null);
//        for (Comment comment : comments) {
//            commentRepository.deleteById(comment.getId());
//        }
//        postLikeRepository.deleteByPost(post);
//        postRepository.deleteById(id);
//        return post;
//    }

    //요청글 전체 조회

    public List<PostResponseDto> getPost() {

        List<Post> posts = postRepository.findAllByStateOrderByCreatedAtDesc(publishing);
        List<PostResponseDto> response = new ArrayList<>();

        for (Post post : posts) {
            Integer answerCount = answerRepository.countByPost(post);
            Long postLikeCount = postLikeRepository.countByPost(post);
            PostResponseDto postResponseDto = new PostResponseDto(
                    post.getId(),
                    post.getTitle(),
                    post.getContent(),
                    post.getModifiedAt(),
                    answerCount,
                    postLikeCount
            );
            response.add(postResponseDto);
        }
        return response;
    }

    //요청글 상세조회.
    public ResponseDto getPost1(Long postId) {

        Post post = postRepository.findById(postId).orElseThrow(
                () -> new PostNotFoundException("해당 글은 존재하지 않습니다.")
        );

        List<PostLike> likeLIst = postLikeRepository.findAllByPost(post);

        List<Long> userIdList = new ArrayList<>();
        for (PostLike postLike : likeLIst) {
            userIdList.add(postLike.getUser().getId());
        }


        Integer answerCount = answerRepository.countByPost(post);
        Long postLikeCount = postLikeRepository.countByPost(post);
        List<PostFile> findPostFileList = postFileRepository.findAllByPostOrderByCreatedAt(post);
        List<String> fileList = new ArrayList<>();
        for (PostFile postFile : findPostFileList) {
            fileList.add(postFile.getUrl());
        }


        ResponseDto ResponseDto = new ResponseDto(
                post.getId(),
                post.getTitle(),
                post.getContent(),
                post.getModifiedAt(),
                answerCount,
                post.getUser().getId(),
                post.getUser().getNickName(),
                postLikeCount,
                userIdList,
                fileList,
                post.getLevel(),
                post.getCategory());






        return ResponseDto;
    }

    //카테고리

    public List<CategoryResponseDto> getCategory(String category) {
        List<Post> posts;
        if (category.equals("all")) {
            posts = postRepository.findAllByStateOrderByCreatedAtDesc(publishing);
        } else {
            posts = postRepository.findAllByCategoryAndStateOrderByCreatedAtDesc(category,publishing);
        }
        List<CategoryResponseDto> response = new ArrayList<>();
        if (posts == null) {
            throw new PostNotFoundException("글이 존재하지 않습니다");
        }
        for (Post post : posts) {
            Integer answerCount = answerRepository.countByPost(post);
            Long postLikeCount = postLikeRepository.countByPost(post);
            CategoryResponseDto categoryResponseDto = new CategoryResponseDto(
                    post.getId(),
                    post.getTitle(),
                    post.getContent(),
                    post.getModifiedAt(),
                    answerCount,
                    postLikeCount

            );
            response.add(categoryResponseDto);
        }
        return response;
    }



}
