package com.extension.test.transactions;

import static com.querydsl.core.types.Projections.constructor;

import com.extension.test.transactions.dto.TransactionHistoryView;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class TransactionRepositoryImpl implements TransactionRepositoryCustom {

  private final JPAQueryFactory queryFactory;

  @Override
  public long sumTodayWithdrawAmount(Long accountId, LocalDateTime from, LocalDateTime to) {
    QTransaction tx = QTransaction.transaction;

    Long sum = queryFactory
        .select(tx.amount.sum())
        .from(tx)
        .where(
            tx.transactionType.eq(TransactionType.WITHDRAW),
            tx.status.eq(TransactionStatusType.SUCCESS),
            tx.fromAccountId.eq(accountId),
            tx.occurredAt.goe(from),
            tx.occurredAt.lt(to)
        )
        .fetchOne();

    return sum == null ? 0L : sum;
  }

  @Override
  public long sumTodayTransferAmount(Long accountId, LocalDateTime from, LocalDateTime to) {
    QTransaction tx = QTransaction.transaction;

    Long sum = queryFactory.select(tx.amount.sum())
        .from(tx)
        .where(
            tx.transactionType.eq(TransactionType.TRANSFER),
            tx.status.eq(TransactionStatusType.SUCCESS),
            tx.fromAccountId.eq(accountId),
            tx.occurredAt.goe(from),
            tx.occurredAt.lt(to)
        )
        .fetchOne();

    return sum == null ? 0L : sum;
  }

  @Override
  public List<TransactionHistoryView> findHistoriesByAccountId(Long accountId, int limit, int offset) {
    QTransaction tx = QTransaction.transaction;

    return queryFactory
        .select(constructor(
            TransactionHistoryView.class,
            tx.id,
            tx.transactionType,
            tx.status,
            tx.amount,
            tx.fee,
            tx.fromAccountId,
            tx.toAccountId,
            tx.occurredAt
        ))
        .from(tx)
        .where(
            tx.fromAccountId.eq(accountId)
                .or(tx.toAccountId.eq(accountId))
        )
        .orderBy(tx.occurredAt.desc(), tx.id.desc())
        .offset(offset)
        .limit(limit)
        .fetch();
  }
}
