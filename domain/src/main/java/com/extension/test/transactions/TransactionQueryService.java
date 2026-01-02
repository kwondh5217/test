package com.extension.test.transactions;

import com.extension.test.accounts.Account;
import com.extension.test.accounts.AccountRepository;
import com.extension.test.exception.AccountNotFoundException;
import com.extension.test.transactions.dto.TransactionHistoryView;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
public class TransactionQueryService {

  private final AccountRepository accountRepository;
  private final TransactionRepository transactionRepository;

  @Transactional(readOnly = true)
  public List<TransactionHistoryView> getHistory(String accountNumber, int limit, int offset) {
    Account account = accountRepository.findByAccountNumber(accountNumber)
        .orElseThrow(() -> new AccountNotFoundException(accountNumber));

    return transactionRepository.findHistoriesByAccountId(account.getId(), limit, offset);
  }
}
