import { api, setToken } from './client';

export async function login(username, password) {
  const data = await api.post('/auth/login', { username, password });
  setToken(data.token);
  return data;
}

export function logout() {
  setToken(null);
}

export function ping() {
  return api.get('/api/ping');
}
