# TradeX React frontend

React 19 + Vite + Lucide icons. Uses the real Spring Boot APIs through `/api`.

Run `npm install && npm run dev` with the API gateway on port 9090. For containerized use, run `docker compose up --build` from the repo root and open http://localhost:3000.

Pages: login/register, dashboard, markets, portfolio, demo wallet, trading modal, and order history. All prices and financial balances come from the backend. No live market feed or actual deposits are supported.
