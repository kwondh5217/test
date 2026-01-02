package com.extension.test.transactions;

import com.extension.test.transactions.dto.TransactionHistoryView;
import java.time.LocalDateTime;
import java.util.List;

public interface TransactionRepositoryCustom {

  long sumTodayWithdrawAmount(Long accountId, LocalDateTime from, LocalDateTime to);

  long sumTodayTransferAmount(Long accountId, LocalDateTime from, LocalDateTime to);

  List<TransactionHistoryView> findHistoriesByAccountId(Long accountId, int limit, int offset);
}

