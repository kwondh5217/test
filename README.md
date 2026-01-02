## 실행

실행 방법
```
docker compose -f tools/docker-compose.yml down -v
docker compose -f tools/docker-compose.yml up --build
```


MySQL 컨테이너가 먼저 기동된 뒤 API 서버가 실행됩니다.

초기 테이블은 `tools/mysql/init.sql` 을 통해 자동 생성됩니다.

---

## API 명세
### 공통 응답 포맷

모든 API는 아래 형식의 응답을 반환합니다.

```json
{
"success": true,
"code": "OK",
"message": null,
"data": {},
"errors": null
}
```

- success: 요청 성공 여부
- code: 응답 코드
- message: 메시지
- data: 응답 데이터
- errors: 필드 단위 에러 정보

---

### 계좌 API
1.계좌 생성

`POST /accounts`

Request Body
```json
{
"accountNumber": "12345678"
}
```

Response (201 Created)
```json
{
"success": true,
"code": "OK",
"data": {
"id": 1,
"accountNumber": "12345678"
}
}
```
예외
- 중복 계좌 번호 → 409 CONFLICT

---

2.계좌 삭제

`DELETE /accounts/{accountNumber}`

Response

204 NO CONTENT

---

### 거래 API
1.입금

`POST /transactions/{accountNumber}/deposit`

Request Body
```json
{
"amount": 1000
}
```


Response (201 Created)

```json
{
  "success": true,
  "data": {
    "transactionId": 1
  }
}
```

---

2.출금

`POST /transactions/{accountNumber}/withdraw`

정책

1일 출금 한도: 1,000,000원

Request Body
```json
{
  "amount": 50000
}
```

예외

- 일 출금 한도 초과 → 400 BAD REQUEST

---

3.이체

`POST /transactions/{accountNumber}/transfer`

정책

- 수수료: 이체 금액의 1%
- 1일 이체 한도: 3,000,000원
- 동일 계좌 이체 불가 

Request Body
```json
{
"toAccountNumber": "87654321",
"amount": 100000
}
```

Response
```json
{
  "success": true,
  "data": {
    "transactionId": 10
  }
}
```

---

### 거래 내역 조회
거래 내역 조회

`GET /transactions/{accountNumber}/history`

Query Parameters
- limit (default: 50)
- offset (default: 0)

Response
```json
{
  "success": true,
  "data": {
    "items": [
      {
        "txId": 1,
        "transactionType": "DEPOSIT",
        "status": "SUCCESS",
        "amount": 1000,
        "fee": 0,
        "fromAccountId": null,
        "toAccountId": 1,
        "occurredAt": "2026-01-02T16:31:40"
      }
    ]
  }
}
```
최신 거래부터 내림차순 정렬됩니다.

---

## 예외 처리 정책

모든 예외는 `ApiExceptionHandler`에서 처리되며,  
응답은 공통 `ApiResponse` 포맷으로 일관되게 반환됩니다.

### 예외 응답 예시

```json
{
  "success": false,
  "code": "INVALID_REQUEST",
  "message": "같은 계좌로 이체할 수 없습니다.",
  "data": null,
  "errors": null
}
```

---

## 동시성 처리 및 트랜잭션 설계

동시에 여러 거래 요청이 발생하는 상황을 고려하여  
데이터 정합성을 보장하도록 설계되었습니다.

### 계좌 락 전략

- 출금, 이체 시 계좌 조회는 **비관적 락(PESSIMISTIC_WRITE)** 을 사용합니다.
- JPA Repository에서 `SELECT ... FOR UPDATE` 방식으로 계좌를 조회합니다.
- 이를 통해 동시에 여러 요청이 들어와도 잔액이 정확히 계산됩니다.

### 이체 시 데드락 방지

이체는 두 계좌를 동시에 수정하므로 데드락 가능성이 존재합니다.  
이를 방지하기 위해 다음 전략을 적용했습니다.

- 계좌 번호 기준으로 정렬하여 락 획득 순서를 고정
- 항상 동일한 순서로 계좌 락을 획득하도록 구현


---

## 테스트 구성

### 통합 테스트

- Spring Context + MySQL(TestContainer) 기반 통합 테스트
- 실제 트랜잭션, 락, 동시성 환경을 최대한 동일하게 검증

### 동시성 테스트

- `ExecutorService` + `CountDownLatch`를 이용한 동시 요청 시뮬레이션
- 입금 / 출금 / 이체 각각에 대해 동시성 테스트 작성
- 일 한도 초과, 수수료 포함 잔액 계산까지 검증


---
