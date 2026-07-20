const BASE_URL = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080';

function getToken() {
  return sessionStorage.getItem('jwt');
}

export function setToken(token) {
  if (token) sessionStorage.setItem('jwt', token);
  else sessionStorage.removeItem('jwt');
}

async function request(path, options = {}) {
  const token = getToken();
  const headers = { 'Content-Type': 'application/json', ...(options.headers || {}) };
  if (token) headers['Authorization'] = `Bearer ${token}`;

  const res = await fetch(`${BASE_URL}${path}`, { ...options, headers });

  // The API protects every non-login route. Spring Security may report an
  // absent, expired, or invalid JWT as either 401 or 403, so clear a stale
  // session in both cases rather than leaving the UI in a broken state.
  if (res.status === 401 || res.status === 403) {
    setToken(null);
    sessionStorage.removeItem('username');
    if (!path.startsWith('/auth/login')) {
      window.location.href = '/login';
    }
    throw new Error('Authentication failed');
  }

  if (res.status === 204) return null;

  let body = null;
  try {
    body = await res.json();
  } catch (e) {
    // no JSON body
  }

  if (!res.ok) {
    const message = (body && (body.error || body.message)) || `Request failed (${res.status})`;
    throw new Error(message);
  }

  return body;
}

export const api = {
  get: (path) => request(path),
  post: (path, data) =>
    request(path, {
      method: 'POST',
      body: data !== undefined ? JSON.stringify(data) : undefined,
    }),
  put: (path, data) => request(path, { method: 'PUT', body: JSON.stringify(data) }),
  patch: (path) => request(path, { method: 'PATCH' }),
};
