package com.ll.demo.global.config;

import com.ll.demo.global.app.AppConfig;
import com.ll.demo.member.entity.Member;
import com.ll.demo.member.repository.MemberRepository;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseCookie;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.SavedRequestAwareAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.Map;

@Component
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CustomAuthenticationSuccessHandler extends SavedRequestAwareAuthenticationSuccessHandler { //로그인 후 추가적인 작업

    private final JwtProperties jwtProperties;
    private final MemberRepository memberRepository;
    private final Oauth2Properties oauth2Properties;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws ServletException, IOException, IOException {
        String redirectUrlAfterSocialLogin = oauth2Properties.getRedirectUrlAfterSocialLogin();
        if (redirectUrlAfterSocialLogin.startsWith(AppConfig.getSiteFrontUrl())){
            String username = authentication.getName();
            Member member = memberRepository.findByUsername(username)
                    .orElseThrow(() -> new UsernameNotFoundException("사용자를 찾을 수 없습니다."));
            String accessToken = JwtUtil.encode(
                    60 * 10, // 100분
                    Map.of(
                            "id", member.getId().toString(),
                            "username", member.getUsername(),
                            "nickname", member.getNickname(),
                            "kakaoLogin", "true",
                            "authorities", member.getAuthoritiesAsStrList()
                    )
                    , jwtProperties.getSecretKey()
            );
            String refreshToken = JwtUtil.encode(
                    60 * 60 * 24, //1 day
                    Map.of(
                            "id", member.getId().toString(),
                            "username", member.getUsername(),
                            "nickname", member.getNickname()
                    )
                    , jwtProperties.getSecretKey()
            );
            request.getSession().invalidate(); //현재 HTTP 세션을 무효화
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
            removeCookie(response,"redirectUrlAfterSocialLogin"); //카카오 로그인과 같은 소셜 로그인 시나리오에서, 로그인 프로세스의 일부로 임시로 설정된 쿠키를 정리하는 데 필요
            response.sendRedirect(redirectUrlAfterSocialLogin);
            return;
        }

        super.onAuthenticationSuccess(request, response, authentication);
    }

    private void removeCookie(HttpServletResponse response,String name) {
        Cookie cookie = new Cookie(name, null);
        cookie.setPath("/");
        cookie.setMaxAge(0);
        response.addCookie(cookie);
    }
}
