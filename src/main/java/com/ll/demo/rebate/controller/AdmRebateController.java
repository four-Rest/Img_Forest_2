package com.ll.demo.rebate.controller;


import com.ll.demo.global.response.GlobalResponse;
import com.ll.demo.order.entity.OrderItem;
import com.ll.demo.order.service.OrderService;
import com.ll.demo.rebate.service.RebateService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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

}
