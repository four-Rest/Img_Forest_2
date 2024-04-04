package com.ll.demo.cash.controller;


import com.ll.demo.cash.entity.WithdrawApply;
import com.ll.demo.cash.service.WithdrawService;
import com.ll.demo.global.response.GlobalResponse;
import com.ll.demo.member.entity.Member;
import com.ll.demo.member.service.MemberService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import retrofit2.http.Path;

import java.security.Principal;
import java.util.List;

@RequestMapping("api/adm/withdraw")
@RestController
@RequiredArgsConstructor
@Slf4j
public class AdmWithdrawController {

    private final WithdrawService withdrawService;
    private final MemberService memberService;

    @GetMapping("/applyList")
    @PreAuthorize("isAuthenticated()")
    public GlobalResponse showApplyList(Principal principal) {
        List<WithdrawApply> withdrawApplies = withdrawService.findAll();
        return GlobalResponse.of("200","조회 성공",withdrawApplies);
    }
    @PostMapping("/{id}/delete")
    @PreAuthorize("isAuthenticated()")
    public GlobalResponse delete(@PathVariable("id") long id, Principal principal) {

        WithdrawApply withdrawApply = withdrawService.findById(id).orElseThrow(() -> new IllegalArgumentException("출금 신청이 존재하지 않습니다."));
        Member member = memberService.findByUsername(principal.getName());
        if(!withdrawService.canDelete(member,withdrawApply)) {
            throw new RuntimeException("출금 신청을 취소할 수 없습니다.");
        }
        withdrawService.delete(withdrawApply);
        return GlobalResponse.of("200","해당 출금 신청이 삭제되었습니다.");
    }
}
