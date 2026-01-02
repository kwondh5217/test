package com.extension.test.api;

import com.extension.test.accounts.Account;
import com.extension.test.accounts.AccountService;
import com.extension.test.api.dto.ApiResponse;
import com.extension.test.api.dto.CreateAccountRequest;
import com.extension.test.api.dto.CreateAccountResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/accounts")
public class AccountController {

  private final AccountService accountService;

  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  public ApiResponse<CreateAccountResponse> create(
      @Valid @RequestBody CreateAccountRequest req
  ) {
    Account account = accountService.createAccount(req.accountNumber());
    return ApiResponse.success(CreateAccountResponse.from(account));
  }

  @DeleteMapping("/{accountNumber}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void delete(@PathVariable @NotBlank String accountNumber) {
    accountService.deleteAccount(accountNumber);
  }
}
