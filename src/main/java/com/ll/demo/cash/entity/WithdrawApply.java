package com.ll.demo.cash.entity;
import com.ll.demo.global.jpa.BaseTime;
import com.ll.demo.member.entity.Member;
import jakarta.persistence.Entity;
import jakarta.persistence.ManyToOne;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;

@Entity
@SuperBuilder
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Setter
@Getter
@ToString(callSuper = true)
public class WithdrawApply extends BaseTime {

    private LocalDateTime withdrawDate;
    private LocalDateTime cancelDate;


    @ManyToOne
    private Member applicant;
    private String bankName;
    private String bankAccountNo;
    private long cash;


}
