package com.extension.test.api.dto;

import jakarta.validation.constraints.Min;

public record DepositRequest(@Min(1) long amount) {

}
