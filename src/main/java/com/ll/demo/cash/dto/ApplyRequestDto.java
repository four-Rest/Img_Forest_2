package com.ll.demo.cash.dto;


import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;

@Getter
public class ApplyRequestDto {

    @NotNull
    private long cash;

    @NotBlank
    private String bankName;

    @NotBlank
    private String bankAccountNo;
}
