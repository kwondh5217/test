package com.extension.test;

import static org.assertj.core.api.Assertions.assertThat;

import com.extension.test.accounts.Account;
import com.extension.test.accounts.AccountRepository;
import com.extension.test.exception.DailyWithdrawLimitExceededException;
import com.extension.test.transactions.TransactionRepository;
import com.extension.test.transactions.TransactionService;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.TestConstructor;

@TestConstructor(autowireMode = TestConstructor.AutowireMode.ALL)
class AccountIntegrationTest extends AbstractIntegrationTest {

  private final TransactionService transactionService;
  private final AccountRepository accountRepository;
  private final TransactionRepository transactionRepository;

  AccountIntegrationTest(
      TransactionService transactionService,
      AccountRepository accountRepository, final TransactionRepository transactionRepository
  ) {
    this.transactionService = transactionService;
    this.accountRepository = accountRepository;
    this.transactionRepository = transactionRepository;
  }

  @BeforeEach
  void setUp() {
    transactionRepository.deleteAll();
    accountRepository.deleteAll();
  }

  @Test
  void concurrent_deposits_are_applied_correctly() throws Exception {
    // given
    String accountNumber = "12345678";
    accountRepository.save(new Account(accountNumber));

    int threads = 20;
    long amount = 1000L;

    ExecutorService pool = Executors.newFixedThreadPool(threads);
    CountDownLatch ready = new CountDownLatch(threads);
    CountDownLatch start = new CountDownLatch(1);
    CountDownLatch done = new CountDownLatch(threads);

    List<Future<?>> futures = new ArrayList<>();

    // when
    for (int i = 0; i < threads; i++) {
      futures.add(pool.submit(() -> {
        ready.countDown();
        try {
          start.await();
          transactionService.deposit(accountNumber, amount);
        } catch (Exception e) {
          throw new RuntimeException(e);
        } finally {
          done.countDown();
        }
      }));
    }

    ready.await();     // 모두 준비될 때까지
    start.countDown(); // 동시에 시작
    done.await();      // 모두 끝날 때까지

    // 예외가 있었으면 여기서 터지게
    for (Future<?> f : futures) f.get();

    // then
    Account reloaded = accountRepository.findByAccountNumber(accountNumber).orElseThrow();
    assertThat(reloaded.getBalance()).isEqualTo(threads * amount);

    pool.shutdownNow();
  }

  @Test
  void concurrent_withdraws_are_applied_correctly_when_balance_is_enough() throws Exception {
    // given
    String accountNumber = "12345678";
    accountRepository.save(new Account(accountNumber));

    int threads = 20;
    long amount = 1_000L;

    transactionService.deposit(accountNumber, threads * amount);

    ExecutorService pool = Executors.newFixedThreadPool(threads);
    CountDownLatch ready = new CountDownLatch(threads);
    CountDownLatch start = new CountDownLatch(1);
    CountDownLatch done = new CountDownLatch(threads);

    List<Future<?>> futures = new ArrayList<>();

    // when
    for (int i = 0; i < threads; i++) {
      futures.add(pool.submit(() -> {
        ready.countDown();
        try {
          start.await();
          transactionService.withdraw(accountNumber, amount);
        } catch (Exception e) {
          throw new RuntimeException(e);
        } finally {
          done.countDown();
        }
      }));
    }

    ready.await();
    start.countDown();
    done.await();

    // 예외가 있었으면 여기서 터지게
    for (Future<?> f : futures) f.get();

    // then
    Account reloaded = accountRepository.findByAccountNumber(accountNumber).orElseThrow();
    assertThat(reloaded.getBalance()).isEqualTo(0L);

    pool.shutdownNow();
  }

  @Test
  void concurrent_withdraws_should_not_exceed_daily_limit() throws Exception {
    // given
    String accountNumber = "12345678";
    accountRepository.save(new Account(accountNumber));

    int threads = 20;
    long amount = 100_000L; // 10번 성공하면 1,000,000

    transactionService.deposit(accountNumber, 10_000_000L);

    ExecutorService pool = Executors.newFixedThreadPool(threads);
    CountDownLatch ready = new CountDownLatch(threads);
    CountDownLatch start = new CountDownLatch(1);
    CountDownLatch done = new CountDownLatch(threads);

    List<Future<?>> futures = new ArrayList<>();

    // when
    for (int i = 0; i < threads; i++) {
      futures.add(pool.submit(() -> {
        ready.countDown();
        try {
          start.await();
          transactionService.withdraw(accountNumber, amount);
        } catch (Exception e) {
          throw new RuntimeException(e);
        } finally {
          done.countDown();
        }
      }));
    }

    ready.await();
    start.countDown();
    done.await();

    int success = 0;
    int failedByLimit = 0;
    List<Throwable> unexpected = new ArrayList<>();

    for (Future<?> f : futures) {
      try {
        f.get();
        success++;
      } catch (ExecutionException ee) {
        Throwable cause = ee.getCause();
        Throwable root = unwrap(cause);

        if (root instanceof DailyWithdrawLimitExceededException) {
          failedByLimit++;
        } else {
          unexpected.add(root);
        }
      }
    }

    // then
    assertThat(unexpected).isEmpty();
    assertThat(failedByLimit).isEqualTo(10);

    long limit = TransactionService.LIMIT;
    long maxSuccess = limit / amount;
    assertThat(success).isEqualTo((int) maxSuccess);

    Account reloaded = accountRepository.findByAccountNumber(accountNumber).orElseThrow();
    long expectedBalance = 10_000_000L - (success * amount);
    assertThat(reloaded.getBalance()).isEqualTo(expectedBalance);

    pool.shutdownNow();
  }

  private static Throwable unwrap(Throwable t) {
    if (t instanceof RuntimeException && t.getCause() != null) {
      return t.getCause();
    }
    return t;
  }
}