package com.extension.test.api.dto;

import jakarta.validation.constraints.NotBlank;

public record CreateAccountRequest(@NotBlank String accountNumber) {

}
