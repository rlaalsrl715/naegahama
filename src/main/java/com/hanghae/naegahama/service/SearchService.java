package com.hanghae.naegahama.service;

import com.hanghae.naegahama.domain.*;
import com.hanghae.naegahama.dto.BasicResponseDto;
import com.hanghae.naegahama.dto.search.SearchAnswerRequest;
import com.hanghae.naegahama.dto.search.SearchPostRequest;
import com.hanghae.naegahama.dto.search.SearchRequest;
import com.hanghae.naegahama.handler.ex.PostNotFoundException;
import com.hanghae.naegahama.repository.*;
import com.hanghae.naegahama.security.UserDetailsImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;


@RequiredArgsConstructor
@Service
@Slf4j
@Transactional
public class SearchService {

    private final PostRepository postRepository;
    private final AnswerRepository answerRepository;
    private final SearchRepository searchRepository;


//    //검색키워드 유저정보에 저장.
//    public List<SearchPostRequestDto> postSearchList(String searchWord, UserDetailsImpl userDetails) {
//        User user = userDetails.getUser();
//        Search search = new Search(searchWord, user);
//        searchRepository.save(search);

    //검색키워드 유저정보에 저장.
    public SearchPostRequest postSearchList(String searchWord, UserDetailsImpl userDetails) {
        log.info("searchWord = {}", searchWord);
        if (!(userDetails == null)) {
            User user = userDetails.getUser();
            if (!searchRepository.existsBySearchWordAndUser(searchWord, user)) ;
            {
                Search search = new Search(searchWord, user);
                searchRepository.save(search);
            }
        }

        //요청글 검색.
        List<Post> posts = postRepository.findAllByTitleContainingOrContentContainingOrderByCreatedAtDesc(searchWord, searchWord);
        List<SearchRequest> searchRequests = new ArrayList<>();

        Integer answerCount = answerRepository.countByContentContainingOrTitleContaining(searchWord, searchWord);
        if (posts.size() == 0) {
            throw new PostNotFoundException("글이 존재하지 않습니다");
        }
        for (Post post : posts) {
            String file = null;
            if (post.getFileList().size() != 0) {
                file = post.getFileList().get(0).getUrl();
            }
            SearchRequest searchRequest = new SearchRequest(
                    post.getId(),
                    post.getTitle(),
                    post.getContent(),
                    post.getModifiedAt(),
                    file
            );
            searchRequests.add(searchRequest);
        }

        SearchPostRequest searchPostRequest = new SearchPostRequest(
                searchRequests,
                answerCount
        );
        return searchPostRequest;
    }


    //답변글 검색
    public SearchAnswerRequest answerSearchList(String searchWord) {

        log.info("searchWord = {}", searchWord);
        List<Answer> Answers = answerRepository.findAllByTitleContainingOrContentContainingOrderByCreatedAtDesc(searchWord, searchWord);
        List<SearchRequest> searchRequests = new ArrayList<>();

        Integer postCount = postRepository.countByContentContainingOrTitleContaining(searchWord, searchWord);
        if (Answers.size() == 0) {
            throw new PostNotFoundException("글이 존재하지 않습니다");
        }
        for (Answer Answer : Answers) {
            String file = null;
            if (Answer.getFileList().size() != 0) {
                file = Answer.getFileList().get(0).getUrl();
            }
            SearchRequest searchRequest = new SearchRequest(
                    Answer.getId(),
                    Answer.getTitle(),
                    Answer.getContent(),
                    Answer.getModifiedAt(),
                    file
            );
            searchRequests.add(searchRequest);
        }

        SearchAnswerRequest searchAnswerRequest = new SearchAnswerRequest(
                searchRequests,
                postCount
        );
        return searchAnswerRequest;
    }
}

//
//    //검색어 삭제
//    public ResponseEntity deleteSearch(Long searchId, UserDetailsImpl userDetails) {
//        User user = userDetails.getUser();
//        searchRepository.deleteByIdAndUser(searchId, user);
//        return ResponseEntity.ok().body(new BasicResponseDto("true"));
//    }
//
//    //검색어 전체삭제
//    public ResponseEntity<?> deleteAllSearch(UserDetailsImpl userDetails) {
//        User user = userDetails.getUser();
//        searchRepository.deleteByUser(user);
//        return ResponseEntity.ok().body(new BasicResponseDto("true"));
//    }
//}
