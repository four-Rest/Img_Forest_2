package com.ll.demo.global.config;

import com.ll.demo.global.rq.Rq;
import com.ll.demo.member.entity.Member;
import com.ll.demo.member.repository.MemberRepository;
import com.ll.demo.member.service.MemberService;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
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

    private final Rq rq;
    private final JwtProperties jwtProperties;
    //private final MemberService memberService;
    private final MemberRepository memberRepository;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws ServletException, IOException, IOException {
        Cookie[] cookies = request.getCookies();
        String redirectUrlAfterSocialLogin = rq.getCookieValue("redirectUrlAfterSocialLogin", "http://localhost:3000");
        System.out.println("redirectUrlAfterSocialLogin !!!!!!!!!!!!" + redirectUrlAfterSocialLogin);
        if (rq.isFrontUrl(redirectUrlAfterSocialLogin)) {
            String username = authentication.getName();
            Member member = memberRepository.findByUsername(username)
                    .orElseThrow(() -> new UsernameNotFoundException("사용자를 찾을 수 없습니다."));
            String accessToken = JwtUtil.encode(
                    60 * 10, // 100분
                    Map.of(
                            "id", member.getId().toString(),
                            "username", member.getUsername(),
                            "authorities", member.getAuthoritiesAsStrList()
                    )
                    , jwtProperties.getSecretKey()
            );
            String refreshToken = JwtUtil.encode(
                    60 * 60 * 24, //1 day
                    Map.of(
                            "id", member.getId().toString(),
                            "username", member.getUsername()
                    )
                    , jwtProperties.getSecretKey()
            );
            rq.destroySession();
            rq.setCrossDomainCookie("accessToken", accessToken);
            rq.setCrossDomainCookie("refreshToken", refreshToken);
            rq.removeCookie("redirectUrlAfterSocialLogin");

            response.sendRedirect(redirectUrlAfterSocialLogin);
            return;
        }

        super.onAuthenticationSuccess(request, response, authentication);
    }
}