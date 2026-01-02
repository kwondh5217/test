package com.extension.test.api.dto;

import jakarta.validation.constraints.Min;

public record WithdrawRequest(@Min(1) long amount) {

}
