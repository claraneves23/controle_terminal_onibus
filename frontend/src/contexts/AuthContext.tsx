import { createContext, useContext, useEffect, useMemo, useState } from 'react';
import type { ReactNode } from 'react';
import type { Perfil, UsuarioLogado } from '../types/api';
import { api, getStoredToken, setStoredToken } from '../services/api';

interface AuthContextValue {
  user: UsuarioLogado | null;
  loading: boolean;
  login: (email: string, senha: string) => Promise<void>;
  logout: () => void;
  hasRole: (...perfis: Perfil[]) => boolean;
}

const AuthContext = createContext<AuthContextValue | null>(null);

export function AuthProvider({ children }: { children: ReactNode }) {
  const [user, setUser] = useState<UsuarioLogado | null>(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const token = getStoredToken();
    if (!token) {
      setLoading(false);
      return;
    }
    api
      .get<UsuarioLogado>('/auth/me')
      .then((resp) => setUser(resp.data))
      .catch(() => {
        setStoredToken(null);
        setUser(null);
      })
      .finally(() => setLoading(false));
  }, []);

  const login = async (email: string, senha: string) => {
    const resp = await api.post('/auth/login', { email, senha });
    setStoredToken(resp.data.accessToken);
    setUser(resp.data.usuario);
  };

  const logout = () => {
    setStoredToken(null);
    setUser(null);
  };

  const hasRole = (...perfis: Perfil[]) =>
    user != null && perfis.includes(user.perfil);

  const value = useMemo<AuthContextValue>(
    () => ({ user, loading, login, logout, hasRole }),
    [user, loading]
  );

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}

export function useAuth() {
  const ctx = useContext(AuthContext);
  if (!ctx) throw new Error('useAuth deve ser usado dentro de <AuthProvider>.');
  return ctx;
}
