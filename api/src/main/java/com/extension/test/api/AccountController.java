package com.extension.test.api;

import com.extension.test.accounts.Account;
import com.extension.test.accounts.AccountService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RestController
@RequestMapping("/accounts")
public class AccountController {

  private final AccountService accountService;

  record CreateAccountRequest(@NotBlank String accountNumber) {}

  record CreateAccountResponse(Long id, String accountNumber) {
    static CreateAccountResponse from(Account a) {
      return new CreateAccountResponse(a.getId(), a.getAccountNumber());
    }
  }

  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  public ApiResponse<CreateAccountResponse> create(
      @Valid @RequestBody CreateAccountRequest req) {

    Account account = accountService.createAccount(req.accountNumber());
    return ApiResponse.success(CreateAccountResponse.from(account));
  }
}
