import { useCallback, useEffect, useMemo, useState } from 'react';
import {
  Activity, ArrowDownLeft, ArrowRight, ArrowUpRight, BarChart3, Bell,
  CheckCircle2, ChevronDown, CircleDollarSign, Clock3, Command,
  CreditCard, History, Layers3, LayoutDashboard, LogOut, Menu, Plus,
  RefreshCcw, Search, ShieldCheck, TrendingDown, TrendingUp, Wallet,
  X, Zap
} from 'lucide-react';
import { api, post, session, currency, signedCurrency, pct, when } from './api.js';

const NAV = [
  { id: 'overview', label: 'Overview', icon: LayoutDashboard },
  { id: 'markets', label: 'Markets', icon: BarChart3 },
  { id: 'portfolio', label: 'My portfolio', icon: Layers3 },
  { id: 'wallet', label: 'Wallet', icon: Wallet },
  { id: 'orders', label: 'Order history', icon: History }
];

function Badge({ children, tone = 'neutral' }) {
  return <span className={'badge badge-' + tone}>{children}</span>;
}
function Delta({ value, unit = 'money' }) {
  const n = Number(value || 0);
  return <span className={n >= 0 ? 'positive' : 'negative'}>{n >= 0 ? <TrendingUp size={15} /> : <TrendingDown size={15} />}{unit === 'pct' ? pct(n) : signedCurrency(n)}</span>;
}
function Empty({ icon: Icon = Layers3, title, description, action }) {
  return <div className="empty"><span className="empty-icon"><Icon size={28} /></span><h3>{title}</h3><p>{description}</p>{action}</div>;
}
function Loading() { return <div className="loading"><RefreshCcw className="spin" size={26}/><span>Syncing your workspace…</span></div>; }

function Auth({ onAuthenticated }) {
  const [mode, setMode] = useState('login');
  const [name, setName] = useState('');
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [busy, setBusy] = useState(false);
  const [error, setError] = useState('');
  async function submit(e) {
    e.preventDefault(); setError(''); setBusy(true);
    try {
      const data = await post('/api/auth/' + (mode === 'login' ? 'login' : 'register'),
        mode === 'login' ? { email, password } : { name, email, password });
      session.save(data);
      onAuthenticated(data);
    } catch (err) { setError(err.message); } finally { setBusy(false); }
  }
  return <div className="auth-shell">
    <div className="auth-art">
      <div className="brand bright"><span className="brand-icon"><Activity size={24}/></span><span>Trade<span className="brand-x">X</span></span></div>
      <div className="auth-hero">
        <Badge tone="primary"><Zap size={12}/> YOUR TRADING SANDBOX</Badge>
        <h1>Markets move.<br/><span>Stay ahead.</span></h1>
        <p>Track equities, build a portfolio, and explore market orders with a modern paper-trading experience.</p>
        <div className="hero-ticker"><div><small>SIMULATED PORTFOLIO</small><strong>Build with confidence.</strong></div><Activity size={48}/></div>
      </div>
      <p className="art-caption"><ShieldCheck size={15}/> Paper trading only · No real money · No exchange orders</p>
    </div>
    <div className="auth-form-side"><div className="auth-panel">
      <div className="eyebrow">WELCOME TO TRADEX</div>
      <h2>{mode === 'login' ? 'Sign in to your account' : 'Create your account'}</h2>
      <p className="muted">{mode === 'login' ? 'Pick up right where you left off.' : 'Start your paper-trading journey.'}</p>
      <div className="auth-toggle"><button className={mode === 'login' ? 'selected' : ''} onClick={() => { setMode('login'); setError(''); }}>Sign in</button><button className={mode === 'register' ? 'selected' : ''} onClick={() => { setMode('register'); setError(''); }}>Create account</button></div>
      <form onSubmit={submit}>
        {mode === 'register' && <label>Full name<input autoComplete="name" required minLength={2} placeholder="Your name" value={name} onChange={e => setName(e.target.value)}/></label>}
        <label>Email address<input type="email" autoComplete="email" required placeholder="you@example.com" value={email} onChange={e => setEmail(e.target.value)}/></label>
        <label>Password<input type="password" autoComplete={mode === 'login' ? 'current-password' : 'new-password'} minLength={8} required placeholder="At least 8 characters" value={password} onChange={e => setPassword(e.target.value)}/></label>
        {error && <div className="alert danger">{error}</div>}
        <button className="btn btn-primary btn-full" disabled={busy}>{busy ? 'Please wait…' : mode === 'login' ? 'Sign in' : 'Create account'}<ArrowRight size={17}/></button>
      </form>
      <div className="auth-note"><ShieldCheck size={16}/> Educational trading simulator. Deposits and trades are not real.</div>
    </div></div>
  </div>;
}

function TradeDialog({ stock, user, onClose, onDone }) {
  const [side, setSide] = useState('BUY');
  const [quantity, setQuantity] = useState('1');
  const [busy, setBusy] = useState(false);
  const [error, setError] = useState('');
  const [success, setSuccess] = useState(null);
  const total = Number(stock.currentPrice || 0) * (Number(quantity) || 0);
  async function submit(e) {
    e.preventDefault(); setError('');
    if (!Number.isSafeInteger(Number(quantity)) || Number(quantity) < 1) { setError('Enter a positive whole number of shares.'); return; }
    setBusy(true);
    try {
      const order = await post('/api/orders/' + side.toLowerCase(), { userId: user.userId, stockSymbol: stock.symbol, quantity: Number(quantity) });
      setSuccess(order);
      await onDone();
    } catch(err) { setError(err.message); } finally { setBusy(false); }
  }
  return <div className="modal-backdrop" role="presentation" onMouseDown={e => { if (e.target === e.currentTarget) onClose(); }}>
    <section className="modal" role="dialog" aria-modal="true" aria-label="Place stock order">
      <div className="modal-heading"><div><span className="eyebrow">TRADE ORDER</span><h2>{stock.symbol} <span className="subtle">· {stock.companyName}</span></h2></div><button className="icon-btn" onClick={onClose} aria-label="Close"><X size={19}/></button></div>
      {success ? <div className="success-state"><CheckCircle2 size={48}/><h2>Order {String(success.status).toLowerCase()}</h2><p>{success.tradeType} {success.quantity} shares of {success.stockSymbol} · {currency(success.totalAmount)}</p><button className="btn btn-primary" onClick={onClose}>Done</button></div> :
      <form onSubmit={submit}>
        <div className="side-switch"><button type="button" className={side === 'BUY' ? 'buy active' : ''} onClick={() => { setSide('BUY'); setError(''); }}>Buy shares</button><button type="button" className={side === 'SELL' ? 'sell active' : ''} onClick={() => { setSide('SELL'); setError(''); }}>Sell shares</button></div>
        <div className="quote-price"><div><small>MARKET PRICE</small><strong>{currency(stock.currentPrice)}</strong></div><Delta value={Number(stock.currentPrice) - Number(stock.previousClose)}/></div>
        <label>Quantity (shares)<input type="number" min="1" step="1" required value={quantity} onChange={e => setQuantity(e.target.value)}/></label>
        <div className="trade-total"><span>Estimated order value</span><strong>{currency(total)}</strong></div>
        <p className="hint">Market orders use the latest simulated stock price. No real orders are placed.</p>
        {error && <div className="alert danger">{error}</div>}
        <button className={'btn btn-full ' + (side === 'BUY' ? 'btn-primary' : 'btn-sell')} disabled={busy || !stock.active}>{busy ? 'Placing order…' : side === 'BUY' ? 'Buy ' + stock.symbol : 'Sell ' + stock.symbol}<ArrowRight size={17}/></button>
      </form>}
    </section>
  </div>;
}

function WalletDeposit({ user, onClose, onDone }) {
  const [amount, setAmount] = useState('10000'); const [busy, setBusy] = useState(false); const [error, setError] = useState('');
  async function submit(e) {
    e.preventDefault(); setBusy(true); setError('');
    try {
      if (Number(amount) < 1 || !Number.isFinite(Number(amount))) throw new Error('Enter a valid amount.');
      await post('/api/wallet/deposit', { userId: user.userId, amount: Number(amount) });
      await onDone(); onClose();
    } catch (err) { setError(err.message); } finally { setBusy(false); }
  }
  return <div className="modal-backdrop" onMouseDown={e => { if (e.target === e.currentTarget) onClose(); }}><div className="modal" role="dialog" aria-modal="true" aria-label="Add demo funds"><div className="modal-heading"><div><span className="eyebrow">DEMO WALLET</span><h2>Add simulated funds</h2></div><button className="icon-btn" onClick={onClose} aria-label="Close"><X size={19}/></button></div><p className="muted">This is an educational simulation. No payment is charged.</p><form onSubmit={submit}><label>Amount (INR)<input type="number" min="1" max="1000000" step="0.01" required value={amount} onChange={e => setAmount(e.target.value)}/></label><div className="quick-amounts">{[5000, 10000, 50000].map(a => <button type="button" onClick={() => setAmount(String(a))} key={a}>{currency(a)}</button>)}</div>{error && <div className="alert danger">{error}</div>}<button className="btn btn-primary btn-full" disabled={busy}>{busy ? 'Adding…' : 'Add demo funds'}<Plus size={17}/></button></form></div></div>;
}

export default function App() {
  const [user, setUser] = useState(() => session.load());
  const [page, setPage] = useState('overview');
  const [stocks, setStocks] = useState([]);
  const [wallet, setWallet] = useState(null);
  const [portfolio, setPortfolio] = useState(null);
  const [orders, setOrders] = useState([]);
  const [transactions, setTransactions] = useState([]);
  const [selectedStock, setSelectedStock] = useState(null);
  const [deposit, setDeposit] = useState(false);
  const [search, setSearch] = useState('');
  const [loading, setLoading] = useState(false);
  const [refreshing, setRefreshing] = useState(false);
  const [notice, setNotice] = useState('');
  const [sidebarOpen, setSidebarOpen] = useState(false);

  const refresh = useCallback(async (initial = false) => {
    if (!user?.userId) return;
    if (initial) setLoading(true); else setRefreshing(true);
    try {
      const [s, w, p, o, tx] = await Promise.allSettled([
        api('/api/stocks'),
        api('/api/wallet/' + user.userId),
        api('/api/portfolio/' + user.userId),
        api('/api/orders/user/' + user.userId),
        api('/api/wallet/transactions/' + user.userId)
      ]);
      if (s.status === 'fulfilled') setStocks(s.value || []);
      if (p.status === 'fulfilled') setPortfolio(p.value);
      if (o.status === 'fulfilled') setOrders(o.value || []);
      if (tx.status === 'fulfilled') setTransactions(tx.value || []);
      if (w.status === 'fulfilled') setWallet(w.value);
      else if (w.reason?.message?.toLowerCase().includes('wallet not found')) {
        try { setWallet(await post('/api/wallet/create', { userId: user.userId })); } catch { /* existing wallet can be fetched on next refresh */ }
      }
      if (s.status === 'rejected') throw s.reason;
    } catch(err) { setNotice(err.message); } finally { setLoading(false); setRefreshing(false); }
  }, [user?.userId]);

  useEffect(() => {
    if (!user) return;
    refresh(true);
    const timer = setInterval(() => refresh(false), 30000);
    return () => clearInterval(timer);
  }, [user, refresh]);
  useEffect(() => {
    const reset = () => { setUser(null); setWallet(null); setPortfolio(null); setOrders([]); setTransactions([]); };
    window.addEventListener('tradex:session-expired', reset);
    return () => window.removeEventListener('tradex:session-expired', reset);
  }, []);
  const matchedStocks = useMemo(() => stocks.filter(s => [s.symbol,s.companyName,s.sector].join(' ').toLowerCase().includes(search.toLowerCase())), [search,stocks]);
  const pnl = Number(portfolio?.totalProfitLoss || 0);
  const cards = [
    { label: 'Portfolio value', value: currency(portfolio?.currentValue), icon: Layers3, delta: <Delta value={pnl}/> },
    { label: 'Available funds', value: currency(wallet?.availableBalance), icon: Wallet, delta: <span className="muted small">For new trades</span> },
    { label: 'Invested amount', value: currency(portfolio?.totalInvested), icon: CircleDollarSign, delta: <span className="muted small">{portfolio?.holdings?.length || 0} stock positions</span> },
    { label: 'Total return', value: pct(portfolio?.totalProfitLossPercentage || 0), icon: TrendingUp, delta: <Delta unit="money" value={pnl}/> }
  ];
  function logout() { session.clear(); setUser(null); setPage('overview'); setWallet(null); setPortfolio(null); setStocks([]); setOrders([]); }
  function navigate(next) { setPage(next); setSearch(''); setSidebarOpen(false); setNotice(''); }
  if (!user) return <Auth onAuthenticated={setUser} />;
  const title = NAV.find(n => n.id === page)?.label || 'Overview';

  return <div className="app">
    <aside className={'sidebar ' + (sidebarOpen ? 'sidebar-open' : '')}>
      <div className="brand"><span className="brand-icon"><Activity size={23}/></span><span>Trade<span className="brand-x">X</span></span></div>
      <div className="sidebar-caption">WORKSPACE</div>
      <nav aria-label="Main navigation">{NAV.map(item => <button className={'nav-item ' + (page === item.id ? 'active' : '')} key={item.id} onClick={() => navigate(item.id)}><item.icon size={19}/><span>{item.label}</span>{page === item.id && <span className="nav-indicator"/>}</button>)}</nav>
      <div className="sidebar-bottom">
        <div className="demo-label"><span className="demo-dot"/> PAPER TRADING</div>
        <div className="help-card"><Zap size={20}/><strong>Invest in practice.</strong><p>Explore the markets without risking capital.</p></div>
        <button className="profile" onClick={logout}><span className="avatar">{(user.email || 'U').charAt(0).toUpperCase()}</span><span><strong>{user.email?.split('@')[0]}</strong><small>Sign out</small></span><LogOut size={17}/></button>
      </div>
    </aside>
    {sidebarOpen && <div className="mobile-scrim" onClick={() => setSidebarOpen(false)}/>}
    <div className="workspace">
      <header className="topbar"><button className="icon-btn mobile-menu" onClick={() => setSidebarOpen(true)} aria-label="Open menu"><Menu size={22}/></button><div className="crumb"><span>Workspace</span><span>/</span><strong>{title}</strong></div><div className="top-actions"><span className="market-pill"><span className="online-dot"/> DEMO MARKET</span><button className="icon-btn" onClick={() => refresh(false)} disabled={refreshing} title="Refresh data" aria-label="Refresh data"><RefreshCcw className={refreshing ? 'spin' : ''} size={19}/></button><span className="avatar small-avatar">{(user.email || 'U').charAt(0).toUpperCase()}</span></div></header>
      <main className="content">
        {notice && <div className="alert danger toast"><span>{notice}</span><button onClick={() => setNotice('')} aria-label="Dismiss"><X size={16}/></button></div>}
        {loading ? <Loading/> : <>
          <div className="page-header"><div><div className="eyebrow"><span className="eyebrow-dot"/> LIVE WORKSPACE · SIMULATED PRICES</div><h1>{page === 'overview' ? 'Good to have you here.' : title}</h1><p>{page === 'overview' ? "Here's what's happening with your portfolio." : page === 'markets' ? 'Explore the stocks available in your trading sandbox.' : page === 'wallet' ? 'Manage your simulated account balance.' : page === 'portfolio' ? 'Your holdings, positions and performance.' : 'A complete record of your simulated trades.'}</p></div><button className="btn btn-outline" onClick={() => refresh(false)} disabled={refreshing}><RefreshCcw size={16} className={refreshing ? 'spin' : ''}/> Refresh</button></div>

          {page === 'overview' && <>
            <div className="hero-banner"><div><Badge tone="primary"><Zap size={13}/> SIMULATED TRADING</Badge><h2>Make your next move<br/><span>with confidence.</span></h2><p>Discover stocks and practice placing orders — all in a risk-free environment.</p><button className="btn btn-white" onClick={() => navigate('markets')}>Explore markets <ArrowUpRight size={17}/></button></div><div className="hero-visual"><div className="visual-orbit"/><Activity size={114} strokeWidth={1.3}/></div></div>
            <div className="stats-grid">{cards.map(({label,value,icon:Icon,delta}) => <div className="stat-card" key={label}><div className="stat-top"><span>{label}</span><span className="stat-icon"><Icon size={19}/></span></div><strong>{value}</strong><div className="stat-foot">{delta}</div></div>)}</div>
            <div className="two-col"><section className="panel"><div className="panel-heading"><div><h3>Watch the market</h3><p>Stock prices available for simulated orders</p></div><button className="link-btn" onClick={() => navigate('markets')}>View all <ArrowRight size={15}/></button></div><div className="table-scroll"><table><thead><tr><th>Asset</th><th>Price</th><th>Change</th><th/></tr></thead><tbody>{stocks.slice(0,6).map(s => <tr key={s.symbol}><td><div className="asset-cell"><span className="asset-icon">{s.symbol.slice(0,2)}</span><span><strong>{s.symbol}</strong><small>{s.companyName}</small></span></div></td><td className="num">{currency(s.currentPrice)}</td><td><Delta unit="pct" value={Number(s.previousClose) ? ((Number(s.currentPrice) - Number(s.previousClose)) / Number(s.previousClose) * 100) : 0}/></td><td><button className="table-action" onClick={() => setSelectedStock(s)}>Trade</button></td></tr>)}</tbody></table>{!stocks.length && <Empty title="No stocks yet" description="An admin can seed or add stocks using the Stock Service API."/>}</div></section>
            <section className="panel"><div className="panel-heading"><div><h3>Recent activity</h3><p>Your latest simulated orders</p></div><button className="link-btn" onClick={() => navigate('orders')}>View all <ArrowRight size={15}/></button></div><div className="activity-list">{orders.slice(0,5).map(o => <div className="activity-row" key={o.orderId}><span className={'activity-icon ' + (o.tradeType === 'BUY' ? 'buy-bg' : 'sell-bg')}>{o.tradeType === 'BUY' ? <ArrowDownLeft size={20}/> : <ArrowUpRight size={20}/>}</span><span className="activity-desc"><strong>{o.tradeType === 'BUY' ? 'Bought' : 'Sold'} {o.stockSymbol}</strong><small>{when(o.createdAt)}</small></span><span className="activity-number"><strong>{currency(o.totalAmount)}</strong><Badge tone={o.status === 'EXECUTED' ? 'success' : 'neutral'}>{o.status}</Badge></span></div>)}{!orders.length && <Empty icon={Clock3} title="No trades yet" description="Your first order will appear here." action={<button className="btn btn-outline" onClick={() => navigate('markets')}>Explore stocks <ArrowRight size={15}/></button>}/>}</div></section></div>
          </>}

          {page === 'markets' && <section className="panel market-panel"><div className="panel-heading"><div><h3>All listed stocks <span className="count">{matchedStocks.length}</span></h3><p>Prices are managed by the demo backend, not a live stock exchange.</p></div><div className="search"><Search size={18}/><input placeholder="Search symbol, company, sector..." aria-label="Search stocks" value={search} onChange={e => setSearch(e.target.value)}/></div></div><div className="table-scroll"><table><thead><tr><th>Company / symbol</th><th>Sector</th><th>Market price</th><th>Previous close</th><th>Change</th><th>Action</th></tr></thead><tbody>{matchedStocks.map(s => <tr key={s.symbol}><td><div className="asset-cell"><span className="asset-icon">{s.symbol.slice(0,2)}</span><span><strong>{s.symbol}</strong><small>{s.companyName}</small></span></div></td><td className="muted">{s.sector || '—'}</td><td className="num strong">{currency(s.currentPrice)}</td><td className="num">{currency(s.previousClose)}</td><td><Delta unit="pct" value={Number(s.previousClose) ? ((Number(s.currentPrice) - Number(s.previousClose)) / Number(s.previousClose) * 100) : 0}/></td><td>{s.active ? <button className="table-action" onClick={() => setSelectedStock(s)}>Trade <ArrowUpRight size={14}/></button> : <Badge>Disabled</Badge>}</td></tr>)}</tbody></table>{!matchedStocks.length && <Empty icon={Search} title="No matching stocks" description={search ? 'Try another keyword.' : 'The stock catalog is empty. Enable demo seed data or add stocks through the administrator API.'}/>}</div></section>}

          {page === 'portfolio' && <>
            <div className="stats-grid three">{[cards[0],cards[2],cards[3]].map(({label,value,icon:Icon,delta}) => <div className="stat-card" key={label}><div className="stat-top"><span>{label}</span><span className="stat-icon"><Icon size={19}/></span></div><strong>{value}</strong><div className="stat-foot">{delta}</div></div>)}</div>
            <section className="panel"><div className="panel-heading"><div><h3>Your holdings <span className="count">{portfolio?.holdings?.length || 0}</span></h3><p>Average acquisition price and unrealized P&amp;L</p></div><button className="link-btn" onClick={() => navigate('markets')}>Buy more <ArrowRight size={15}/></button></div><div className="table-scroll"><table><thead><tr><th>Stock</th><th>Shares</th><th>Avg. buy</th><th>Latest value*</th><th>Unrealized P&amp;L</th><th/></tr></thead><tbody>{(portfolio?.holdings || []).map(h => <tr key={h.id}><td><div className="asset-cell"><span className="asset-icon">{h.stockSymbol.slice(0,2)}</span><strong>{h.stockSymbol}</strong></div></td><td className="num">{h.quantity}</td><td className="num">{currency(h.averageBuyPrice)}</td><td className="num">{currency(h.currentValue)}</td><td><Delta value={h.profitLoss}/></td><td><button className="table-action" onClick={() => { const stock = stocks.find(s => s.symbol === h.stockSymbol); if (stock) setSelectedStock(stock); else setNotice('This stock is not in the current catalog.'); }}>Trade</button></td></tr>)}</tbody></table>{!portfolio?.holdings?.length && <Empty title="Your portfolio is waiting" description="Buy a stock to add your first holding." action={<button className="btn btn-outline" onClick={() => navigate('markets')}>Find a stock <ArrowRight size={15}/></button>}/>}</div><p className="hint table-hint">*Holding prices reflect the last price recorded for each position and may differ from the latest catalog quote.</p></section>
          </>}

          {page === 'wallet' && <><div className="wallet-hero"><div><small>TOTAL SIMULATED BALANCE</small><h2>{currency(wallet?.balance)}</h2><p><span className="online-dot"/> Available to trade: {currency(wallet?.availableBalance)}</p></div><button className="btn btn-white" onClick={() => setDeposit(true)}><Plus size={18}/> Add demo funds</button></div><div className="stats-grid two"><div className="stat-card"><div className="stat-top"><span>Available balance</span><Wallet size={18}/></div><strong>{currency(wallet?.availableBalance)}</strong><div className="stat-foot muted">Usable for new orders</div></div><div className="stat-card"><div className="stat-top"><span>Reserved for orders</span><Clock3 size={18}/></div><strong>{currency(wallet?.blockedAmount)}</strong><div className="stat-foot muted">Pending trade allocation</div></div></div><section className="panel"><div className="panel-heading"><div><h3>Wallet activity</h3><p>All simulated funds movements</p></div></div><div className="table-scroll"><table><thead><tr><th>Transaction</th><th>When</th><th>Amount</th><th>Balance after</th></tr></thead><tbody>{transactions.map(t => <tr key={t.id}><td><strong>{String(t.transactionType).replaceAll('_',' ')}</strong><small className="table-sub">{t.description}</small></td><td className="muted">{when(t.createdAt)}</td><td className="num">{currency(t.amount)}</td><td className="num">{currency(t.balanceAfterTransaction)}</td></tr>)}</tbody></table>{!transactions.length && <Empty icon={CreditCard} title="No wallet activity" description="Add demo funds to get started."/>}</div></section></>}

          {page === 'orders' && <section className="panel"><div className="panel-heading"><div><h3>All orders <span className="count">{orders.length}</span></h3><p>Your buys, sells and order execution status</p></div></div><div className="table-scroll"><table><thead><tr><th>Stock</th><th>Side</th><th>Quantity</th><th>Price</th><th>Total</th><th>Status</th><th>Placed at</th></tr></thead><tbody>{orders.map(o => <tr key={o.orderId}><td><strong>{o.stockSymbol}</strong></td><td><Badge tone={o.tradeType === 'BUY' ? 'success' : 'warning'}>{o.tradeType}</Badge></td><td className="num">{o.quantity}</td><td className="num">{currency(o.price)}</td><td className="num strong">{currency(o.totalAmount)}</td><td><Badge tone={o.status === 'EXECUTED' ? 'success' : o.status === 'FAILED' ? 'danger' : 'neutral'}>{o.status}</Badge>{o.failureReason && <small className="table-sub">{o.failureReason}</small>}</td><td className="muted">{when(o.createdAt)}</td></tr>)}</tbody></table>{!orders.length && <Empty icon={History} title="No orders placed" description="Orders will appear here when you buy or sell stocks." action={<button className="btn btn-outline" onClick={() => navigate('markets')}>Start trading <ArrowRight size={15}/></button>}/>}</div></section>}
        </>}
        <footer className="footer"><span><Activity size={16}/> TradeX © 2026</span><span>For education and demonstration only. Not investment advice or a brokerage.</span></footer>
      </main>
    </div>
    {selectedStock && <TradeDialog stock={selectedStock} user={user} onClose={() => setSelectedStock(null)} onDone={refresh}/>}
    {deposit && <WalletDeposit user={user} onClose={() => setDeposit(false)} onDone={refresh}/>}
  </div>;
}
