import { createContext, useContext, useState, useCallback } from 'react';
import * as authApi from '../api/auth';

const AuthContext = createContext(null);

function readStoredUser() {
  const raw = localStorage.getItem('user');
  if (!raw) return null;
  try {
    return JSON.parse(raw);
  } catch {
    return null;
  }
}

export function AuthProvider({ children }) {
  const [user, setUser] = useState(readStoredUser);

  const persist = (auth) => {
    const nextUser = {
      userId: auth.userId,
      email: auth.email,
      fullName: auth.fullName,
      role: auth.role,
    };
    localStorage.setItem('token', auth.token);
    localStorage.setItem('refreshToken', auth.refreshToken);
    localStorage.setItem('user', JSON.stringify(nextUser));
    setUser(nextUser);
    return nextUser;
  };

  const login = useCallback(async (email, password) => {
    const auth = await authApi.login(email, password);
    return persist(auth);
  }, []);

  const register = useCallback(async (fullName, email, password) => {
    const auth = await authApi.register(fullName, email, password);
    return persist(auth);
  }, []);

  const logout = useCallback(() => {
    const refreshToken = localStorage.getItem('refreshToken');
    if (refreshToken) {
      authApi.logout(refreshToken).catch(() => {});
    }
    localStorage.removeItem('token');
    localStorage.removeItem('refreshToken');
    localStorage.removeItem('user');
    setUser(null);
  }, []);

  const updateUser = useCallback((patch) => {
    setUser((prev) => {
      const next = { ...prev, ...patch };
      localStorage.setItem('user', JSON.stringify(next));
      return next;
    });
  }, []);

  return (
    <AuthContext.Provider
      value={{
        user,
        login,
        register,
        logout,
        updateUser,
        isAdmin: user?.role === 'ADMIN',
        isStaff: user?.role === 'CAREER_SUPPORT_STAFF',
      }}
    >
      {children}
    </AuthContext.Provider>
  );
}

export function useAuth() {
  const ctx = useContext(AuthContext);
  if (!ctx) throw new Error('useAuth must be used within AuthProvider');
  return ctx;
}
