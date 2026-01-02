package com.extension.test.api.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

public record TransferRequest(
    @NotBlank String toAccountNumber,
    @Min(1) long amount
) {}
