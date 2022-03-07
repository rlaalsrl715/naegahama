package com.hanghae.naegahama.dto.user;


import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class UserInfoRequestDto {
    private String category;
    private String gender;
    private String age;
    private String nickname;
}
