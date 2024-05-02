package com.ll.demo.cash.entity;
import com.ll.demo.global.jpa.BaseTime;
import com.ll.demo.member.entity.Member;
import jakarta.persistence.Entity;
import jakarta.persistence.ManyToOne;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

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
    public boolean isWithdrawDone() {
        return withdrawDate != null;
    }

    public void setWithdrawDone() {
        withdrawDate = LocalDateTime.now();
    }

    public String getForPrintWithdrawStatus() {
        if (withdrawDate != null)
            return "처리완료(" + withdrawDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")) + ")";

        if (withdrawDate == null) return "-";

        return "처리가능";
    }

}
