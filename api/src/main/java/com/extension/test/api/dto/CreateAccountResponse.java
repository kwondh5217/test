package com.extension.test.api.dto;

import com.extension.test.accounts.Account;

public record CreateAccountResponse(Long id, String accountNumber) {

  public static CreateAccountResponse from(Account a) {
    return new CreateAccountResponse(a.getId(), a.getAccountNumber());
  }
}
