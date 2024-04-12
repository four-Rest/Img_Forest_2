package com.ll.demo.member.controller;

import com.ll.demo.global.config.JwtProperties;
import com.ll.demo.global.config.JwtUtil;
import com.ll.demo.global.response.GlobalResponse;
import com.ll.demo.member.dto.*;
import com.ll.demo.member.entity.Member;
import com.ll.demo.member.service.MemberService;
import com.ll.demo.member.service.EmailService;
import io.jsonwebtoken.Claims;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.WebUtils;

import java.security.Principal;
import java.util.Arrays;
import java.util.Date;
import java.util.Map;
import java.util.Optional;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/member")
@Tag(name = "Member", description = "Member API")
@ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "요청에 성공하였습니다.", content = @Content(mediaType = "application/json")),
        @ApiResponse(responseCode = "400", description = "요청에 실패했습니다.", content = @Content(mediaType = "application/json")),
        @ApiResponse(responseCode = "401", description = "인증이 필요합니다.", content = @Content(mediaType = "application/json")),
        @ApiResponse(responseCode = "403", description = "요청이 거부되었습니다.", content = @Content(mediaType = "application/json")),
        @ApiResponse(responseCode = "404", description = "리소스를 서버에서 찾을 수 없습니다.", content = @Content(mediaType = "application/json"))
})
public class MemberController {

    private final MemberService memberService;
    private final HttpServletResponse response;
    private final HttpServletRequest request;
    private final JwtProperties jwtProperties;
    private final EmailService emailService;

    @PostMapping("/signup")
    @Operation(summary = "회원가입", description = "회원가입 시 사용하는 API")
    public GlobalResponse signup(@RequestBody MemberCreateRequestDto userCreateRequestDto) {
        if (!userCreateRequestDto.getPassword1().equals(userCreateRequestDto.getPassword2())) {
            return GlobalResponse.of("409", "비밀번호가 일치하지 않습니다");
        }
        return memberService.signup(userCreateRequestDto);
    }

    @PostMapping("/login")
    @Operation(summary = "로그인", description = "로그인 시 사용하는 API")
    public GlobalResponse<LoginResponseDto> login(@RequestBody LoginRequestDto dto) {
        GlobalResponse<Member> checkedResp = memberService.checkMembernameAndPassword(dto.getUsername(), dto.getPassword());
        Member member = checkedResp.getData();
        // accessToken 생성
        String accessToken = JwtUtil.encode(
                60 * 10, // 100분
                Map.of(
                        "id", member.getId().toString(),
                        "username", member.getUsername(),
                        "authorities", member.getAuthoritiesAsStrList()
                )
                , jwtProperties.getSecretKey()
        );
        System.out.println("accessToken 통과");
        // refreshToken 생성 accessToken의 유효 시간이 만료되었을 때 새로운 accessToken 발급 받기 위해 사용
        String refreshToken = JwtUtil.encode(
                60 * 60 * 24, //1 day
                Map.of(
                        "id", member.getId().toString(),
                        "username", member.getUsername()
                )
                , jwtProperties.getSecretKey() // JWT를 생성할 때 사용하는 비밀키
        );
        memberService.setRefreshToken(member, refreshToken);


        // accessToken, refreshToken
        addCrossDomainCookie(accessToken, refreshToken);

        return GlobalResponse.of("200", "로그인 성공.", new LoginResponseDto(member));
    }

    @PostMapping("/login/refresh")
    @Operation(summary = "리프레쉬토큰 발급", description = "리프레쉬 토큰 발급 시 사용하는 API")
    public GlobalResponse refreshAccessToken() {
        Cookie[] cookies = request.getCookies();
        if (cookies == null) {
            return GlobalResponse.of("401", "cookies not exist.");
        }
        Optional<Cookie> refreshTokenCookieOp = Arrays.stream(cookies)
                .filter(cookie -> cookie.getName().equals("refreshToken"))
                .findFirst();
        if (refreshTokenCookieOp.isEmpty()) {
            return GlobalResponse.of("401", "refreshToken not exist.");
        }
        System.out.println("통과");
        String refreshToken = refreshTokenCookieOp.get().getValue();
        Member member = memberService.findUserByRefreshToken(refreshToken).get();
        String accessToken = JwtUtil.encode(
                60 * 10,
                Map.of(
                        "id", member.getId().toString(),
                        "username", member.getUsername(),
                        "authorities", member.getAuthoritiesAsStrList()
                ),
                jwtProperties.getSecretKey()
        );
        ResponseCookie accessCookie = ResponseCookie.from("accessToken", accessToken)
                .path("/")
                .maxAge(60 * 10)
                .sameSite("None")
                .secure(true)
                .httpOnly(true)
                .build();
        response.addHeader("Set-Cookie", accessCookie.toString());
        return GlobalResponse.of("200", "refresh accessToken complete");
    }

    @PostMapping("/logout")
    @Operation(summary = "로그아웃", description = "로그아웃 시 사용하는 API")
    public GlobalResponse logout() {

        removeCrossDomainCookie();
        return GlobalResponse.of("200", "로그아웃 성공");
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/mypage")
    @Operation(summary = "마이페이지", description = "마이페이지 시 사용하는 API")
    public GlobalResponse<MyPageResponseDto> mypage(Principal principal) {

        String username = principal.getName();
        MyPageResponseDto responseDto = new MyPageResponseDto(memberService.findByUsername(username));
        return GlobalResponse.of("200", "유저 정보 반환", responseDto);
    }


    @PreAuthorize("isAuthenticated()")
    @PostMapping("/update")
    @Operation(summary = "내정보수정", description = "내정보수정 시 사용하는 API")
    public GlobalResponse updateMemberInfo(Principal principal, @RequestBody MemberInfoUpdateRequestDto requestDto) {
        String username = principal.getName();
        Member member = memberService.findByUsername(username);
        if (member != null) {
            return memberService.updateMemberInfo(member, requestDto);
        } else {
            return GlobalResponse.of("403", "확인되지 않은 유저입니다.");
        }
    }

    @PostMapping("/checkAccessToken")
    @Operation(summary = "토큰 검증", description = "JWT 검증 시 사용하는 API")
    public GlobalResponse<LoginResponseDto> checkAccessToken(HttpServletRequest request) {
        try {
            Cookie cookie = WebUtils.getCookie(request, "accessToken");
            if (cookie == null) {
                return GlobalResponse.of("401", "없는 토큰");
            }
            String token = cookie.getValue();
            Claims claims = JwtUtil.decode(token, jwtProperties.getSecretKey());

            Date expiration = claims.getExpiration();
            if (expiration.before(new Date())) {
                System.err.println("Token expired");
                return GlobalResponse.of("401", "토큰 만료");
            }
            Map<String, Object> data = (Map<String, Object>) claims.get("data");
            String username = (String) data.get("username");
            System.out.println("kakaoLogin == " + data.get("kakaoLogin"));
            Member member = memberService.findByUsername(username);
            if(data.get("kakaoLogin").equals("true")){
                return GlobalResponse.of("200", "로그인 성공.", new LoginResponseDto(member, (String) data.get("kakaoLogin")));
            }
            else return GlobalResponse.of("200", "로그인 성공.", new LoginResponseDto(member));
        } catch (Exception e) {
            // 다른 예외들 (토큰 만료, 지원하지 않는 JWT 등)
            System.err.println("Token validation error: " + e.getMessage());
            return GlobalResponse.of("401", "지원하지 않는 토큰");
        }
    }

    private void removeCrossDomainCookie() {
        ResponseCookie cookie1 = ResponseCookie.from("accessToken", null)
                .path("/")
                .maxAge(0)
                .sameSite("None")
                .secure(true)
                .httpOnly(true)
                .build();
        ResponseCookie cookie2 = ResponseCookie.from("refreshToken", null)
                .path("/")
                .maxAge(0)
                .sameSite("None")
                .secure(true)
                .httpOnly(true)
                .build();
        response.addHeader("Set-Cookie", cookie1.toString());
        response.addHeader("Set-Cookie", cookie2.toString());
    }

    private void addCrossDomainCookie(String accessToken, String refreshToken) {
        ResponseCookie cookie1 = ResponseCookie.from("accessToken", accessToken)
                .path("/")
                .maxAge(60 * 10)
                .sameSite("None")
                .secure(true)
                .httpOnly(true)
                .build();
        ResponseCookie cookie2 = ResponseCookie.from("refreshToken", refreshToken)
                .path("/")
                .maxAge(60 * 60 * 24)
                .sameSite("None")
                .secure(true)
                .httpOnly(true)
                .build();
        response.addHeader("Set-Cookie", cookie1.toString());
        response.addHeader("Set-Cookie", cookie2.toString());
    }

    //인증 번호 전송
    @PostMapping("/sendEmail")
    public GlobalResponse<MemberEmailRequestDto> sendEmail(@RequestBody MemberEmailRequestDto requestDto) {
        memberService.sendCodeToEmail(requestDto.getEmail());
        System.out.println("이메일 전송 완료");
        return GlobalResponse.of("200", "이메일 전송 성공");
    }

    //이메일 인증
    @PostMapping("/verifyEmail")
    public GlobalResponse<MemberEmailVerifyResponseDto> verifyEmail(@RequestBody MemberEmailVerifyRequestDto requestDto) {
        boolean isVerified = memberService.verifyCode(requestDto.getEmail(), requestDto.getVerificationCode());
        MemberEmailVerifyResponseDto responseDto = new MemberEmailVerifyResponseDto();
        responseDto.setVerified(isVerified);
        responseDto.setMessage(isVerified ? "Email verified successfully." : "Invalid or expired verification code.");
        if(isVerified) return GlobalResponse.of("200", "인증 완료", responseDto);
        else return GlobalResponse.of("200", "인증 실패", responseDto);
    }
}