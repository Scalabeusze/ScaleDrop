import React, { useState, useEffect } from 'react';
import { AuthContext } from './authContext';

export const AuthProvider = ({ children }) => {
  // Pobierz token z localStorage na starcie, jeśli istnieje
  const [token, setToken] = useState(() => localStorage.getItem('jwt_token'));
  const [user, setUser] = useState(null);
  const logout = () => {
    localStorage.removeItem('jwt_token');
    setToken(null);
    setUser(null);
  };

  useEffect(() => {
    if (token) {
      try {
        const base64Url = token.split('.')[1];
        const base64 = base64Url.replace(/-/g, '+').replace(/_/g, '/');
        const jsonPayload = decodeURIComponent(
          atob(base64)
            .split('')
            .map((c) => '%' + ('00' + c.charCodeAt(0).toString(16)).slice(-2))
            .join('')
        );
        const decoded = JSON.parse(jsonPayload);

        // Call setUser asynchronously to avoid synchronous state update inside effect
        setTimeout(() => {
          setUser({
            accountId: decoded.sub,
            name: decoded.name || decoded.sub || 'User',
            iat: decoded.iat,
            exp: decoded.exp,
          });
        }, 0);
      } catch (error) {
        console.error('Błąd podczas dekodowania tokenu JWT:', error);
        // Avoid calling setState synchronously inside effect; schedule logout asynchronously
        setTimeout(() => logout(), 0);
      }
    }
  }, [token]);

  const login = (data) => {
    // Jeśli `data` to poprawna odpowiedź z BFF
    if (data && data.jwt) {
      localStorage.setItem('jwt_token', data.jwt);
      setToken(data.jwt);
    } else if (typeof data === 'string') {
      // Fallback dla lokalnego mockowania ('user')
      setUser({ name: data });
    }
  };


  return (
    <AuthContext.Provider value={{ token, user, login, logout, isAuthenticated: !!token || !!user }}>
      {children}
    </AuthContext.Provider>
  );
};

// Note: `useAuth` hook lives in `src/context/useAuth.jsx` to keep this file exporting only components (fast-refresh friendly).