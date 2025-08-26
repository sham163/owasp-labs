import React, { createContext, useContext, useEffect, useState } from 'react';
import { api } from './api';

const AuthCtx = createContext(null);
export const useAuth = () => useContext(AuthCtx);

export function AuthProvider({ children }) {
  const [user, setUser] = useState(null);
  const [ready, setReady] = useState(false);

  const refresh = async () => {
    try { setUser(await api('/users/me')); } catch { setUser(null); }
    setReady(true);
  };

  useEffect(() => { refresh(); }, []);

  const login = async (username, password, rememberMe) => {
    const res = await api('/auth/login', {
      method: 'POST', body: JSON.stringify({ username, password, rememberMe })
    });
    if (res.status === 'ok') await refresh(); else throw new Error('Bad creds');
  };

  const register = async (username, password, email) => {
    const res = await api('/auth/register', {
      method: 'POST', body: JSON.stringify({ username, password, email })
    });
    if (res.status !== 'ok') throw new Error('Cannot register');
  };

  const logout = async () => {
    try { await api('/auth/logout', { method: 'POST' }); } catch {}
    // best-effort client cleanup
    document.cookie = 'rememberMe=; Max-Age=0; path=/';
    document.cookie = 'JSESSIONID=; Max-Age=0; path=/';
    setUser(null);
  };

  return (
    <AuthCtx.Provider value={{ user, ready, login, register, logout, refresh }}>
      {children}
    </AuthCtx.Provider>
  );
}
