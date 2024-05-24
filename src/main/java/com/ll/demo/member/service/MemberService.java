package com.ll.demo.member.service;

import com.ll.demo.cash.entity.CashLog;
import com.ll.demo.cash.service.CashService;
import com.ll.demo.global.config.JwtProperties;
import com.ll.demo.global.response.GlobalResponse;
import com.ll.demo.member.dto.*;
import com.ll.demo.member.entity.Member;
import com.ll.demo.member.entity.VerificationCode;
import com.ll.demo.member.repository.EmailRepository;
import com.ll.demo.member.repository.MemberRepository;
import jakarta.mail.MessagingException;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

@Service
@RequiredArgsConstructor
public class MemberService {
    private final MemberRepository memberRepository;
    private final PasswordEncoder encoder;
    private final JwtProperties jwtProperties;
    private final EmailRepository emailRepository;
    private final EmailService emailService;
    private final CashService cashService;

    @Transactional
    public GlobalResponse<Member> signup(MemberCreateRequestDto dto) {
        if (validDuplicationUsername(dto.getUsername()).isPresent()) {
            return GlobalResponse.of("409", "중복된 이름입니다.");
        }
        Member member = Member.builder()
                .username(dto.getUsername())
                .password(encoder.encode(dto.getPassword2()))
                .nickname(dto.getNickname())
                .email(dto.getEmail()).build();

        memberRepository.save(member);
        return GlobalResponse.of("200", "회원가입 완료");
    }

    @Transactional
    public GlobalResponse<Member> socialSignup(kakkoMemberCreateRequestDto dto) {
        if (validDuplicationUsername(dto.getUsername()).isPresent()) {
            return GlobalResponse.of("409", "중복된 이름입니다.");
        }
        Member member = Member.builder()
                .username(dto.getUsername())
                .nickname(dto.getNickname())
                .build();

        memberRepository.saveAndFlush(member);
        return GlobalResponse.of("200", "회원가입 완료", member);
    }

    @Transactional
    public MemberInfoUpdateResponseDto getMemberInfo(Member member) {
        MemberInfoUpdateResponseDto dto = new MemberInfoUpdateResponseDto();
        dto.setLoginId(member.getUsername());
        dto.setEmail(member.getEmail());
        dto.setNickname(member.getNickname());

        return dto;
    }

    @Transactional
    public GlobalResponse<Member> updateMemberInfo(Member member, MemberInfoUpdateRequestDto requestDto) {
        String msg = "";
        if(requestDto.getUsername()!=null && !requestDto.getUsername().isEmpty()) {
            member.setUsername(requestDto.getUsername());
        }

        if (requestDto.getNickname() != null && !requestDto.getNickname().isEmpty()) {
            member.setNickname(requestDto.getNickname());
            msg += "닉네임, ";
        }
        if (requestDto.getPassword2() != null && !requestDto.getPassword2().isEmpty()) {
            member.setPassword(encoder.encode(requestDto.getPassword2()));
            msg += "비밀번호, ";
        }
        if (requestDto.getEmail() != null && !requestDto.getEmail().isEmpty()) {
            member.setEmail(requestDto.getEmail());
            msg += "이메일 ";
        }
        memberRepository.save(member);
        return GlobalResponse.of("200", msg + "변경 완료", member);
    }

    public Optional<Member> validDuplicationUsername(String username) {
        return memberRepository.findByUsername(username);
    }

    public GlobalResponse<Member> checkMembernameAndPassword(String username, String password) {
        Optional<Member> userOp = memberRepository.findByUsername(username);

        if (userOp.isEmpty()) {
            throw new IllegalArgumentException("존재하지 않는 회원입니다.");
        }

        if (!encoder.matches(password, userOp.get().getPassword())) {
            throw new IllegalArgumentException("비밀번호가 일치하지 않습니다.");
        }

        return GlobalResponse.of("200", "로그인 성공", userOp.get());
    }

    public void setRefreshToken(Member member, String refreshToken) {
        memberRepository.save(member.toBuilder().refreshToken(refreshToken).build());
    }

    public Optional<Member> findUserByRefreshToken(String refreshToken) {
        return memberRepository.findByRefreshToken(refreshToken);
    }

    public Member findByUsername(String username){
        Optional<Member> userOp = memberRepository.findByUsername(username);
        if(userOp.isPresent()){
            return userOp.get();
        }
        return null;
    }

    public GlobalResponse updateUserData(Member member, MyPageRequestDto dto) {
        String msg ="";
        if(dto.getNickname() != ""){
            member = member.toBuilder().nickname(dto.getNickname()).build();
            msg += "닉네임, ";
        }
        if(dto.getPassword1() != "" && dto.getPassword2() != ""){
            member = member.toBuilder().password(encoder.encode(dto.getPassword1())).build();
            msg += "비밀번호 ";
        }
        memberRepository.save(member);
        return GlobalResponse.of("200",msg + "변경완료");
    }

    @Transactional
    public GlobalResponse<Member> whenSocialLogin(String providerTypeCode, String username, String nickname, String profileImgUrl) {
        Optional<Member> opMember = validDuplicationUsername(username);

        if (opMember.isPresent()) return GlobalResponse.of("200", "이미 존재합니다.", opMember.get());

        kakkoMemberCreateRequestDto dto = new kakkoMemberCreateRequestDto();
        dto.setUsername(username);
        dto.setNickname(nickname);
        return socialSignup(dto);
    }

    public void sendCodeToEmail(String email) {
        VerificationCode createdCode = createVerificationCode(email);
        String title = "Img Forest 이메일 인증 번호";

        String content = "<html>"
                + "<body>"
                + "<h1>ImgForest 인증 코드: " + createdCode.getCode() + "</h1>"
                + "<p>해당 코드를 홈페이지에 입력하세요.</p>"
                // 푸터 추가
                + "<footer style='color: grey; font-size: small;'>"
                + "<p>※본 메일은 자동응답 메일이므로 본 메일에 회신하지 마시기 바랍니다.</p>"
                + "</footer>"
                + "</body>"
                + "</html>";
        try {
            emailService.sendEmail(email, title, content);
        } catch (RuntimeException | MessagingException e) {
            e.printStackTrace(); // 또는 로거를 사용하여 상세한 예외 정보 로깅
            throw new RuntimeException("Unable to send email in sendCodeToEmail", e); // 원인 예외를 포함시키기
        }
    }

    // 인증 코드 생성 및 저장
    public VerificationCode createVerificationCode(String email) {
        String randomCode = generateRandomCode(6);
        VerificationCode code = VerificationCode.builder()
                .email(email)
                .code(randomCode) // 랜덤 코드 생성
                .expiresTime(LocalDateTime.now().plusDays(1)) // 1일 후 만료
                .build();

        return emailRepository.save(code);
    }

    public String generateRandomCode(int length) {
        // 숫자 + 대문자 + 소문자
        String characters = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        StringBuilder sb = new StringBuilder();
        ThreadLocalRandom random = ThreadLocalRandom.current();

        for (int i = 0; i < length; i++) {
            int index = random.nextInt(characters.length());
            sb.append(characters.charAt(index));
        }

        return sb.toString();
    }

    // 인증 코드 유효성 검사
    public boolean verifyCode(String email, String code) {
        return emailRepository.findByEmailAndCode(email, code)
                .map(vc -> vc.getExpiresTime().isAfter(LocalDateTime.now()))
                .orElse(false);
    }

    @Transactional
    @Scheduled(cron = "0 0 12 * * ?") // 매일 정오에 해당 만료 코드 삭제
    public void deleteExpiredVerificationCodes() {
        emailRepository.deleteByExpiresTimeBefore(LocalDateTime.now());
    }


    @Transactional
    public void addCash(Member member, long price, CashLog.EvenType eventType) {
        CashLog cashLog = cashService.addCash(member, price, eventType);

        long newRestCash = member.getRestCash() + cashLog.getPrice();
        member.setRestCash(newRestCash);
    }
}
