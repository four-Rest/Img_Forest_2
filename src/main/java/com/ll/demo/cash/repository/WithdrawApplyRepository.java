package com.ll.demo.cash.repository;


import com.ll.demo.cash.entity.WithdrawApply;
import com.ll.demo.member.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface WithdrawApplyRepository extends JpaRepository<WithdrawApply, Long> {
    List<WithdrawApply> findByApplicantOrderByIdDesc(Member applicant);
    List<WithdrawApply> findAllByOrderByIdDesc();
}