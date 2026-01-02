package com.extension.test.transactions.dto;


import com.extension.test.transactions.TransactionStatusType;
import com.extension.test.transactions.TransactionType;
import java.time.LocalDateTime;

public record TransactionHistoryView(
    Long txId,
    TransactionType transactionType,
    TransactionStatusType status,
    Long amount,
    Long fee,
    Long fromAccountId,
    Long toAccountId,
    LocalDateTime occurredAt
) {

}