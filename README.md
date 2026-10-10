# TradeX — full-stack stock trading simulator

Java 21 + Spring Boot microservices + Spring Cloud Gateway + Eureka + MySQL + React/Vite.

TradeX is an **educational paper-trading platform**, not a brokerage. Stock prices are fictional, static demo data. Wallet funds are simulated; it never moves real money or places orders on an exchange.

## Implemented user journey

1. Register a user or sign in.
2. Explore demo stocks on the responsive React dashboard.
3. A demo wallet is created on first sign-in. Add simulated funds.
4. Submit a market BUY order: funds are reserved, the holding is updated, and funds are debited.
5. Submit a market SELL order: shares are reduced and wallet funds credited.
6. Review wallet activity, order history, portfolio positions, and performance.

The UI reads real values from the backend; there is no mock frontend API. Portfolio values are based on the holding's last recorded price, not streaming market quotes.

## Start locally with Docker

Requires Docker Engine/Desktop with Compose and enough memory for seven Java services plus MySQL.

```bash
git clone https://github.com/naveed24/stock-trading-platform.git
cd stock-trading-platform
cp .env.example .env
```

Edit `.env` and enter a strong unique `DB_PASSWORD` and a unique `JWT_SECRET` (at least 32 random bytes; `openssl rand -hex 48` is suitable). **Do not use any prior JWT key from Git history.**

```bash
docker compose up --build -d
docker compose ps
```

The first build downloads Maven and npm dependencies, and services can take time to start.

- Frontend: http://localhost:3000
- API gateway: http://localhost:9090
- Stop: `docker compose down`
- Erase **all** demo data: `docker compose down -v` (destructive).

MySQL and every individual backend service remain private to the Docker Compose network. The gateway enforces JWT validation, user ownership for outward-facing APIs, and ADMIN authorization for stock mutations. Internal wallet settlement and portfolio mutation endpoints are not exposed through the gateway.

**Development configuration:** Each Maven module in `backend/` can also be started independently with Java 21 and MySQL. Configure `DB_PASSWORD` and `JWT_SECRET`; the gateway serves on 9090. For frontend hot-reload, `cd frontend && npm install && npm run dev` (http://localhost:5173). Only run direct backend ports on a trusted local machine; service-to-service operations do not have independent authorization.

## Architecture

```text
Browser → Nginx / React UI → Spring Cloud Gateway :9090
                                 ├─ auth-service :8081    → auth_db
                                 ├─ stock-service :8083   → stock_db
                                 ├─ wallet-service :8084  → wallet_db
                                 ├─ trading-service :8085 → trading_db
                                 └─ portfolio-service :8086 → portfolio_db
            Eureka discovery :8761        MySQL :3306 (private)
```

Backend modules: `discovery-server`, `api-gateway`, `auth-service`, `stock-service`, `wallet-service`, `trading-service`, `portfolio-service`.

## API examples

All use `http://localhost:9090`.

- `POST /api/auth/register` with `{"name":"Demo","email":"demo@example.com","password":"unique-password"}`
- `POST /api/auth/login` with `{"email":"demo@example.com","password":"unique-password"}`
- `GET /api/stocks`, `GET /api/stocks/{symbol}`, `GET /api/stocks/search?keyword=TCS`
- `POST /api/wallet/create`, `POST /api/wallet/deposit` with a JSON `userId` matching the authenticated JWT subject
- `GET /api/wallet/{userId}`, `GET /api/wallet/transactions/{userId}`
- `POST /api/orders/buy`, `POST /api/orders/sell` with `{"userId":1,"stockSymbol":"TCS","quantity":1}`
- `GET /api/orders/user/{userId}` and `GET /api/portfolio/{userId}`

Protected calls require an `Authorization: Bearer <accessToken>` header after login.

## Testing

- `backend`: seven independent Maven modules; GitHub Actions compiles all seven.
- `frontend`: `npm install && npm run build`; GitHub Actions builds the React UI.
- Regression tests should be run against a disposable MySQL environment before deployment.
- A successful compile does not establish business-transaction correctness.

## Safety and remaining limitations

**Local, educational demo only. Do not put this on the public internet or use real financial data.**

- Gateway-level JWT and ownership checks depend on all downstream service ports remaining inaccessible externally. Direct service-to-service endpoints are currently trusted within the Docker network; they do not implement service identity verification.
- Trades call multiple databases over HTTP; their updates are **not atomic**. A partial failure, timeout, retry, concurrent order, or process crash can leave wallet and portfolio records inconsistent. Before any serious use, implement a durable Saga/outbox, idempotency keys, compensation and concurrency controls.
- JWT signing keys must be rotated from the hardcoded key published in prior Git commits. Set a new secret; old tokens will become invalid.
- Demo prices are static and not licensed market feeds. Holdings don't automatically revalue until prices are updated.
- The project is not a real-money wallet, investment adviser, stock exchange, or production brokerage.
- CI is a build gate, not a complete automated test suite.

## Branch strategy

Work is submitted through reviewed pull requests. See the GitHub Actions tab for build results, and keep production deployment disabled until the limitations above are resolved.
