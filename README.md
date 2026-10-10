# TradeX — Stock Trading Platform

TradeX is a **simulated** stock-trading platform built with Java 21, Spring Boot, Spring Cloud Gateway, Netflix Eureka, Spring Data JPA and MySQL. This repository currently contains the backend services. It does **not** execute trades on a real exchange or handle real-money payments.

> **Project status (October 2026):** Core backend APIs are implemented, but the end-to-end application is not production-ready. The React frontend, robust authorization, distributed transaction recovery, comprehensive tests and deployment tooling are still pending. See "Known gaps" below.

## Backend services

| Service | Port | Database | Purpose |
| --- | ---: | --- | --- |
| Discovery Server | 8761 | — | Service registration through Eureka |
| API Gateway | 9090 | — | Entry point for backend API requests |
| Auth Service | 8081 | `auth_db` | User registration, login, JWT issuance |
| Stock Service | 8083 | `stock_db` | Stock catalog, search, prices |
| Wallet Service | 8084 | `wallet_db` | Simulated deposits, withdrawals, balance holds and history |
| Trading Service | 8085 | `trading_db` | Market BUY/SELL order processing and order history |
| Portfolio Service | 8086 | `portfolio_db` | Holdings, valuations and transactions |

## Current API surface

All examples use the gateway at `http://localhost:9090`:

- `POST /api/auth/register`, `POST /api/auth/login`
- `GET /api/stocks`, `GET /api/stocks/{symbol}`, `GET /api/stocks/search?keyword=TCS`
- `POST /api/wallet/create`, `POST /api/wallet/deposit`, `GET /api/wallet/{userId}`
- `POST /api/orders/buy`, `POST /api/orders/sell`, `GET /api/orders/user/{userId}`
- `GET /api/portfolio/{userId}`, `GET /api/portfolio/{userId}/holdings/{stockSymbol}`

Some additional internal mutation endpoints exist. **Do not expose this implementation publicly:** authentication tokens are generated but not consistently enforced at the gateway or within business services.

## Local setup (backend)

**Prerequisites:** Java 21, Maven (or the Maven Wrapper included with each service), MySQL, and ports 8761, 9090, 8081, 8083, 8084, 8085, 8086 available.

1. Create the MySQL databases: `auth_db`, `stock_db`, `wallet_db`, `trading_db`, `portfolio_db`.
2. **Before any public use**, replace the example credentials and signing key currently hardcoded in `application.yml` with environment-specific secrets. The already committed JWT signing key must be treated as compromised and rotated.
3. Run `backend/discovery-server` first, then the other services, with the gateway last. Each is an independent Maven project. For example, from a service folder, use `./mvnw spring-boot:run` on Linux/macOS or `mvnw.cmd spring-boot:run` on Windows.
4. Check `http://localhost:8761` for Eureka registrations and `http://localhost:9090/api/stocks/status` for a gateway-to-service status response.
5. Register a user, create the user wallet, add simulated funds, create stocks, and test BUY/SELL requests in an isolated local environment.

**Important:** The current API accepts user IDs in request bodies and URLs without complete access-control enforcement. Use only fake users and simulated funds until that is fixed.

## Known gaps / completion roadmap

1. **Security first:** Move secrets into environment variables; add JWT validation at the gateway and authorization/ownership checks in each backend; secure internal-only portfolio and wallet operations; lock down admin stock mutations and Actuator.
2. **Trading correctness:** Introduce idempotency keys and order state transitions, durable compensation/Saga logic for partial BUY/SELL failures, and concurrency control for wallet funds and holdings.
3. **Quality:** Add unit, integration and end-to-end tests, including negative scenarios; verify all Maven builds in CI.
4. **Frontend:** Add and connect the React/Vite UI (login, dashboard, stock search, buy/sell, wallet, portfolio, orders and charts). **No frontend directory is present in the GitHub main branch as of this audit.**
5. **Developer experience:** Add local configuration examples, seed data, Docker Compose, CI/CD, API documentation, and deploy instructions.
6. **Advanced features (optional):** Kafka event-driven updates, market-data simulation, price-history charts and monitoring.

## Project structure

```text
backend/
  discovery-server/
  api-gateway/
  auth-service/
  stock-service/
  wallet-service/
  trading-service/
  portfolio-service/
```

Each backend service has its own `pom.xml`. The frontend has not yet been committed to this repository.

