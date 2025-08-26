export const api = async (path, opts = {}) => {
  const r = await fetch(`/api${path}`, {
    headers: { 'Content-Type': 'application/json', ...(opts.headers || {}) },
    ...opts
  });
  const ct = r.headers.get('content-type') || '';
  if (!r.ok) throw new Error(`HTTP ${r.status}`);
  return ct.includes('application/json') ? r.json() : r.text();
};
