# Event Ledger API

A RESTful API for processing financial transaction events built with **Java 17**, **Spring Boot 3**, and an **H2 in-memory database**.

Handles:
- ✅ **Idempotency** — duplicate event submissions are safely ignored
- ✅ **Out-of-order events** — events always returned in chronological order by `eventTimestamp`
- ✅ **Balance computation** — accurate net balance regardless of arrival order
- ✅ **Input validation** — clear error messages for bad requests
- ✅ **Pagination** — optional pagination on event listing
- ✅ **Swagger UI** — interactive API documentation

---

## Prerequisites

| Tool | Version |
|------|---------|
| Java | 17+ |
| Maven | 3.8+ |

Check your versions:
```bash
java -version
mvn -version
```

---

## Setup & Run

**1. Clone the repository:**
```bash
git clone https://github.com/kamalkant-dev/event-ledger-api.git
cd event-ledger-api
```

**2. Start the application:**
```bash
mvn spring-boot:run
```

The API will be available at: `http://localhost:8080`

---

## Run Tests

```bash
mvn test
```

---

## API Endpoints

| Method | Endpoint | Description |
|--------|----------|-------------|
| `POST` | `/events` | Submit a transaction event |
| `GET` | `/events/{id}` | Get a single event by ID |
| `GET` | `/events?account={accountId}` | List events for an account (chronological order) |
| `GET` | `/events?account={accountId}&page=0&size=10` | Paginated event listing |
| `GET` | `/accounts/{accountId}/balance` | Get computed balance for an account |

---

## Event Payload

```json
{
  "eventId": "evt-001",
  "accountId": "acct-123",
  "type": "CREDIT",
  "amount": 150.00,
  "currency": "USD",
  "eventTimestamp": "2026-05-15T14:02:11Z",
  "metadata": {
    "source": "mainframe-batch",
    "batchId": "B-9042"
  }
}
```

| Field | Type | Required | Notes |
|-------|------|----------|-------|
| `eventId` | string | Yes | Unique identifier |
| `accountId` | string | Yes | Account this event belongs to |
| `type` | string | Yes | `CREDIT` or `DEBIT` |
| `amount` | number | Yes | Must be > 0 |
| `currency` | string | Yes | e.g. `USD` |
| `eventTimestamp` | ISO 8601 string | Yes | When the event occurred |
| `metadata` | object | No | Optional key-value pairs |

---

## API Documentation (Swagger)

Once running, visit:
```
http://localhost:8080/swagger-ui/index.html
```

## H2 Console (Dev)

```
http://localhost:8080/h2-console
JDBC URL: jdbc:h2:mem:ledgerdb
Username: sa
Password: (leave blank)
```

---

## Design Decisions

- **Idempotency**: On duplicate `POST /events`, the original event is returned with `HTTP 200`. The balance is never double-counted.
- **Out-of-order tolerance**: Events are stored with their original `eventTimestamp` and always queried sorted by it — arrival order is irrelevant.
- **Concurrency**: Per-eventId locking via `ConcurrentHashMap` prevents race conditions on simultaneous duplicate POSTs.
- **Balance computation**: Done via SQL `SUM` aggregation for accuracy and performance.
- **In-memory DB**: H2 is used — no external database setup required.
