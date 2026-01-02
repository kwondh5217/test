package com.extension.test.api;

import com.extension.test.api.dto.ApiResponse;
import com.extension.test.api.dto.DepositRequest;
import com.extension.test.api.dto.DepositResponse;
import com.extension.test.api.dto.TransactionHistoryItem;
import com.extension.test.api.dto.TransactionHistoryResponse;
import com.extension.test.api.dto.TransferRequest;
import com.extension.test.api.dto.TransferResponse;
import com.extension.test.api.dto.WithdrawRequest;
import com.extension.test.api.dto.WithdrawResponse;
import com.extension.test.transactions.TransactionQueryService;
import com.extension.test.transactions.TransactionService;
import com.extension.test.transactions.dto.TransactionHistoryView;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RestController
@RequestMapping("/transactions")
public class TransactionController {

    private final TransactionService transactionService;
    private final TransactionQueryService transactionQueryService;

    @PostMapping("/{accountNumber}/deposit")
    public ResponseEntity<ApiResponse<DepositResponse>> deposit(
            @PathVariable String accountNumber,
            @Valid @RequestBody DepositRequest req
    ) {
        Long txId = transactionService.deposit(accountNumber, req.amount());
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success(new DepositResponse(txId)));
    }

    @PostMapping("/{accountNumber}/withdraw")
    public ResponseEntity<ApiResponse<WithdrawResponse>> withdraw(
        @PathVariable String accountNumber,
        @Valid @RequestBody WithdrawRequest req
    ) {
        Long txId = transactionService.withdraw(accountNumber, req.amount());
        return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(ApiResponse.success(new WithdrawResponse(txId)));
    }

    @PostMapping("/{accountNumber}/transfer")
    public ResponseEntity<ApiResponse<TransferResponse>> transfer(
        @PathVariable String accountNumber,
        @Valid @RequestBody TransferRequest req
    ) {
        Long txId = transactionService.transfer(accountNumber, req.toAccountNumber(), req.amount());
        return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(ApiResponse.success(new TransferResponse(txId)));
    }

    @GetMapping("/{accountNumber}/history")
    public ResponseEntity<ApiResponse<TransactionHistoryResponse>> history(
        @PathVariable String accountNumber,
        @RequestParam(defaultValue = "50") int limit,
        @RequestParam(defaultValue = "0") int offset
    ) {
        List<TransactionHistoryView> views =
            transactionQueryService.getHistory(accountNumber, limit, offset);

        List<TransactionHistoryItem> items = views.stream()
            .map(v -> new TransactionHistoryItem(
                v.txId(),
                v.transactionType(),
                v.status(),
                v.amount(),
                v.fee(),
                v.fromAccountId(),
                v.toAccountId(),
                v.occurredAt()
            ))
            .toList();

        return ResponseEntity.ok(ApiResponse.success(new TransactionHistoryResponse(items)));
    }
}