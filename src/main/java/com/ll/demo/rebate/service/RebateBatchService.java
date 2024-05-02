package com.ll.demo.rebate.service;


import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.stereotype.Service;

import java.time.YearMonth;

@Service
@RequiredArgsConstructor
public class RebateBatchService {
    private final JobLauncher jobLauncher;
    private final Job makeRebateDataJob;

    @SneakyThrows
    public void make(String yearMonth) {

        String[] yearMonthBits = yearMonth.split("-", 2);

        int year = Integer.parseInt(yearMonthBits[0]);
        int month = Integer.parseInt(yearMonthBits[1]);

        YearMonth yearMonth_ = YearMonth.of(year, month); // 연월 지정

        String startDate = yearMonth_ + "-01 00:00:00.000000";
        String endDate = yearMonth_ + "-%02d 23:59:59.999999".formatted(yearMonth_.lengthOfMonth());

        JobParameters jobParameters = new JobParametersBuilder()
                .addString("startDate", startDate)
                .addString("endDate", endDate)
                .toJobParameters();
        jobLauncher.run(makeRebateDataJob, jobParameters);
    }
}
