package com.extension.test.exception;

import lombok.Getter;

@Getter
public class DailyWithdrawLimitExceededException extends RuntimeException {

  private final long limit;
  private final long attemptedAmount;
  private final long todayWithdrawSum;

  public DailyWithdrawLimitExceededException(
      long limit,
      long todayWithdrawSum,
      long attemptedAmount
  ) {
    super("일 출금 한도를 초과했습니다.");
    this.limit = limit;
    this.todayWithdrawSum = todayWithdrawSum;
    this.attemptedAmount = attemptedAmount;
  }
}
