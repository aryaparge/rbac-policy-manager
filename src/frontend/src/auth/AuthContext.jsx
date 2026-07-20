import { createContext, useContext, useState } from 'react';
import { login as loginApi, logout as logoutApi } from '../api/auth';

const AuthContext = createContext(null);

export function AuthProvider({ children }) {
  const [username, setUsername] = useState(() => sessionStorage.getItem('username'));

  async function login(user, pass) {
    await loginApi(user, pass);
    sessionStorage.setItem('username', user);
    setUsername(user);
  }

  function logout() {
    logoutApi();
    sessionStorage.removeItem('username');
    setUsername(null);
  }

  return (
    <AuthContext.Provider value={{ username, isAuthenticated: !!username, login, logout }}>
      {children}
    </AuthContext.Provider>
  );
}

export function useAuth() {
  return useContext(AuthContext);
}
