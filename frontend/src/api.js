const STORAGE_KEY = 'tradex.demo.session';

export const session = {
  load() {
    try {
      const value = sessionStorage.getItem(STORAGE_KEY);
      return value ? JSON.parse(value) : null;
    } catch {
      return null;
    }
  },
  save(value) { sessionStorage.setItem(STORAGE_KEY, JSON.stringify(value)); },
  clear() { sessionStorage.removeItem(STORAGE_KEY); }
};

export async function api(path, options = {}) {
  const token = session.load()?.accessToken;
  const headers = { ...(options.body ? { 'Content-Type': 'application/json' } : {}), ...options.headers };
  if (token) headers.Authorization = 'Bearer ' + token;
  let response;
  try {
    response = await fetch(path, { ...options, headers });
  } catch {
    throw new Error('Cannot reach TradeX. Check that the gateway is running.');
  }
  const raw = await response.text();
  let body;
  try { body = raw ? JSON.parse(raw) : null; } catch { body = raw; }
  if (!response.ok) {
    if (response.status === 401 && !path.startsWith('/api/auth/')) {
      session.clear();
      window.dispatchEvent(new Event('tradex:session-expired'));
    }
    const detail = typeof body === 'object' && body !== null
      ? (body.message || body.error || body.detail)
      : (typeof body === 'string' ? body : null);
    throw new Error(detail || ('Request failed (' + response.status + ')'));
  }
  return body;
}
export const post = (path, value) => api(path, { method: 'POST', body: JSON.stringify(value) });
export const currency = value => '₹' + Number(value || 0).toLocaleString('en-IN', { minimumFractionDigits: 2, maximumFractionDigits: 2 });
export const signedCurrency = value => (Number(value) >= 0 ? '+' : '−') + currency(Math.abs(Number(value || 0)));
export const pct = value => (Number(value) >= 0 ? '+' : '') + Number(value || 0).toFixed(2) + '%';
export const when = value => value ? new Date(value).toLocaleString('en-IN', { dateStyle: 'medium', timeStyle: 'short' }) : '—';
