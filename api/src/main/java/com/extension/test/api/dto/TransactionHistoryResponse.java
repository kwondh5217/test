package com.extension.test.api.dto;

import java.util.List;

public record TransactionHistoryResponse(
    List<TransactionHistoryItem> items
) {

}
