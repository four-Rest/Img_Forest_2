package com.ll.demo.member.dto;

import com.ll.demo.member.entity.Member;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LoginResponseDto {
    private String username;
    private String nickname;
    private String kakaoLogin;

    public LoginResponseDto(Member member) {
        this.username  = member.getUsername();
        this.nickname = member.getNickname();
    }

    public LoginResponseDto(Member member, String kakaoLogin) {
        this.username  = member.getUsername();
        this.nickname = member.getNickname();
        this.kakaoLogin = kakaoLogin;
    }


}
