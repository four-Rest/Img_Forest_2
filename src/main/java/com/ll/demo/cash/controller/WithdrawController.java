package com.ll.demo.cash.controller;


import com.ll.demo.cash.dto.ApplyRequestDto;
import com.ll.demo.cash.entity.WithdrawApply;
import com.ll.demo.cash.service.WithdrawService;
import com.ll.demo.global.response.GlobalResponse;
import com.ll.demo.member.entity.Member;
import com.ll.demo.member.service.MemberService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/withdraw")
@Slf4j
@RequiredArgsConstructor
public class WithdrawController {

    private final WithdrawService withdrawService;
    private final MemberService memberService;


    // 출금신청조회
    @GetMapping("/applyList")
    @PreAuthorize("isAuthenticated()")
    public GlobalResponse showApplyList(Principal principal) {
        Member applicant = memberService.findByUsername(principal.getName());
        List<WithdrawApply> withdrawApplies = withdrawService.findByApplicant(applicant);
        return GlobalResponse.of("200","조회 성공",withdrawApplies);
    }

    // 출금신청
    @PostMapping("/apply")
    @PreAuthorize("isAuthenticated()")
    public GlobalResponse apply(Principal principal, ApplyRequestDto applyRequestDto) {
        Member applicant = memberService.findByUsername(principal.getName());

        if(!withdrawService.canApply(applicant, applyRequestDto.getCash())) {
            throw new RuntimeException("출금 신청이 불가합니다.");
        }

        withdrawService.apply(applicant,applyRequestDto);

        return GlobalResponse.of("200","출금신청이 완료되었습니다.");
    }

    // 출금신청취소
    @PostMapping("/{id}/delete")
    @PreAuthorize("isAuthenticated()")
    public GlobalResponse delete(@PathVariable("id") long id,  Principal principal){
        Member member = memberService.findByUsername(principal.getName());
        WithdrawApply withdrawApply = withdrawService.findById(id).orElseThrow(() -> new IllegalArgumentException(" 출금 신청이 존재하지 않습니다."));

        if(!withdrawService.canDelete(member,withdrawApply)) {
            throw new RuntimeException("출금 신청 취소가 불가합니다.");
        }
        withdrawService.delete(withdrawApply);

        return GlobalResponse.of("200","출금 신청 취소가 완료되었습니다.");
    }
}
