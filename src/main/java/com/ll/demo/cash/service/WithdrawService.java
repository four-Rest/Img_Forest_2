package com.ll.demo.cash.service;


import com.ll.demo.cash.dto.ApplyRequestDto;
import com.ll.demo.cash.entity.CashLog;
import com.ll.demo.cash.entity.WithdrawApply;
import com.ll.demo.cash.repository.WithdrawApplyRepository;
import com.ll.demo.member.entity.Member;
import com.ll.demo.member.service.MemberService;
import jakarta.transaction.TransactionScoped;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class WithdrawService {
    private final WithdrawApplyRepository withdrawApplyRepository;
    private final MemberService memberService;

    public boolean canApply(Member actor, long cash) {
        return actor.getRestCash() >= cash;
    }

    @Transactional
    public void apply(Member applicant,ApplyRequestDto applyRequestDto) {
        WithdrawApply apply = WithdrawApply.builder()
                .applicant(applicant)
                .cash(applyRequestDto.getCash())
                .bankName(applyRequestDto.getBankName())
                .bankAccountNo(applyRequestDto.getBankAccountNo())
                .build();
        withdrawApplyRepository.save(apply);
    }

    public List<WithdrawApply> findByApplicant(Member applicant) {
        return withdrawApplyRepository.findByApplicantOrderByIdDesc(applicant);
    }

    public Optional<WithdrawApply> findById(long id) {
        return withdrawApplyRepository.findById(id);
    }

    public boolean canDelete(Member actor, WithdrawApply withdrawApply) {
        if(withdrawApply.isWithdrawDone()) return false;

        if(actor.isAdmin()) return true;

        if(!withdrawApply.getApplicant().equals(actor)) return false;

        return true;
    }

    @Transactional
    public void delete(WithdrawApply withdrawApply) {
        withdrawApplyRepository.delete(withdrawApply);
    }


    public List<WithdrawApply> findAll() {
        return withdrawApplyRepository.findAllByOrderByIdDesc();
    }

    public boolean canWithdraw(Member actor, WithdrawApply withdrawApply) {

        if(withdrawApply.isWithdrawDone())  return false;

        if(!actor.isAdmin()) return false;

        if(withdrawApply.getApplicant().getRestCash() < withdrawApply.getCash()) return false;

        return true;
    }


    @Transactional
    public void withdraw(WithdrawApply withdrawApply) {
        withdrawApply.setWithdrawDone();
        memberService.addCash(withdrawApply.getApplicant(),withdrawApply.getCash() * -1, CashLog.EvenType.출금__통장입금);
    }
}
