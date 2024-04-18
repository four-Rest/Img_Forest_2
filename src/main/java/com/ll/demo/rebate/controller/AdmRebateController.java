package com.ll.demo.rebate.controller;


import com.ll.demo.global.response.GlobalResponse;
import com.ll.demo.global.util.Ut;
import com.ll.demo.rebate.entity.RebateItem;
import com.ll.demo.rebate.service.RebateService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/adm/rebate")
@RequiredArgsConstructor
public class AdmRebateController {

    private final RebateService rebateService;

    @PostMapping("/make")
    @PreAuthorize("isAuthenticated()")
    public GlobalResponse make(@RequestBody Map<String, String> requestBody) {
        String yearMonth = requestBody.get("yearMonth");
        rebateService.make(yearMonth);
        return GlobalResponse.of("200","정산 데이터 생성 완료");
    }

    @GetMapping("/list/{yearMonth}")
    @PreAuthorize("isAuthenticated()")
    public GlobalResponse showList(@PathVariable("yearMonth") String yearMonth) {

        if (Ut.str.isBlank(yearMonth)) {
            yearMonth = Ut.date.getCurrentYearMonth();
        }
        List<RebateItem> items = rebateService.findByPayDateIn(yearMonth);

        return GlobalResponse.of("200","정산데이터 리스트 반환 완료",items);
    }

    // 단건 정산
    @PostMapping("/{id}/rebate")
    public GlobalResponse rebate(@PathVariable("id") long id) {
        RebateItem rebateItem = rebateService.findById(id).orElseThrow(() -> new RuntimeException("정산 데이터가 존재하지 않습니다."));
        rebateService.rebate(rebateItem);

        return GlobalResponse.of("200","%d번 정산 데이터를 처리했습니다.".formatted(rebateItem.getId()));
    }

}
