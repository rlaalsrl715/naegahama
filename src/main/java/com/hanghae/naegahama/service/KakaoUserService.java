package com.hanghae.naegahama.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hanghae.naegahama.domain.Achievement;
import com.hanghae.naegahama.domain.User;
import com.hanghae.naegahama.domain.UserRoleEnum;
import com.hanghae.naegahama.dto.login.LoginResponseDto;
import com.hanghae.naegahama.dto.user.KakaoUserInfoDto;
import com.hanghae.naegahama.repository.userrepository.UserQuerydslRepository;
import com.hanghae.naegahama.repository.userrepository.UserRepository;
import com.hanghae.naegahama.security.UserDetailsImpl;
import com.hanghae.naegahama.security.jwt.JwtTokenUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import javax.servlet.http.HttpServletResponse;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class KakaoUserService {
    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;
    private final UserQuerydslRepository userQuerydslRepository;


    @Transactional
    public LoginResponseDto kakaoLogin(String accessToken,HttpServletResponse response) throws JsonProcessingException {
        log.info("accessToken = {}",accessToken);
        // 2. 토큰으로 카카오 API 호출
        KakaoUserInfoDto kakaoUserInfoDto =getKakaoUserInfo(accessToken);

        // 3. 필요시에 회원가입
        User kakaoUser = registerKakaoUserIfNeeded(kakaoUserInfoDto);

        // 4. 강제 로그인 처리
        return forceLogin(kakaoUser,response);
        // return getLoginResponseDtoResponseEntity(kakaoUser);

    }
   /* private ResponseEntity<?> getLoginResponseDtoResponseEntity(User user) {
        LoginResponseDto loginResponseDto = new LoginResponseDto(user.getId(),user.getUserStatus());
        return ResponseEntity.ok().body(loginResponseDto);
    }*/



    private KakaoUserInfoDto getKakaoUserInfo(String accessToken) throws JsonProcessingException {
        // HTTP Header 생성
        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", "Bearer " + accessToken);
        headers.add("Content-type", "application/x-www-form-urlencoded;charset=utf-8");

        // HTTP 요청 보내기
        HttpEntity<MultiValueMap<String, String>> kakaoUserInfoRequest = new HttpEntity<>(headers);
        RestTemplate rt = new RestTemplate();
        ResponseEntity<String> response = rt.exchange(
                "https://kapi.kakao.com/v2/user/me",
                HttpMethod.POST,
                kakaoUserInfoRequest,
                String.class
        );

        String responseBody = response.getBody();
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode jsonNode = objectMapper.readTree(responseBody);
        Long id = jsonNode.get("id").asLong();
        String nickname = jsonNode.get("properties")
                .get("nickname").asText();

        System.out.println("카카오 사용자 정보: " + id + ", " + nickname);
        return new KakaoUserInfoDto(id, nickname);
    }

    private User registerKakaoUserIfNeeded(KakaoUserInfoDto kakaoUserInfoDto)
    {
        // DB 에 중복된 Kakao Id 가 있는지 확인
        Long kakaoId = kakaoUserInfoDto.getId();
        User kakaoUser = userQuerydslRepository.findUserByKakaoId(kakaoId)
                .orElse(null);
        if (kakaoUser == null) {
            // 회원가입
            // username: kakao nickname
            String nickname = kakaoUserInfoDto.getNickname();

            // password: random UUID
            String password = UUID.randomUUID().toString();
            String encodedPassword = passwordEncoder.encode(password);

            // email: kakao email
            String email = UUID.randomUUID().toString()+"@hippomail.co.kr";
            // role: 일반 사용자
            UserRoleEnum role = UserRoleEnum.USER;

            kakaoUser = userRepository.save(new User(encodedPassword, email, role, kakaoId));
            kakaoUser.setAchievement(new Achievement());
            userRepository.save(kakaoUser);
        }
        return kakaoUser;
    }

    private LoginResponseDto forceLogin(User kakaoUser, HttpServletResponse response) {
        UserDetailsImpl userDetails = new UserDetailsImpl(kakaoUser);
        Authentication authentication = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(authentication);
        String token = JwtTokenUtils.generateJwtToken(userDetails);
        response.addHeader("Authorization", "Bearer " +token);
        return new LoginResponseDto(kakaoUser.getId(),token,kakaoUser.getUserStatus() );
    }
}
