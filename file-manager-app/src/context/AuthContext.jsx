import React, { createContext, useContext, useState, useEffect } from 'react';

const AuthContext = createContext(null);

export const AuthProvider = ({ children }) => {
  // Pobierz token z localStorage na starcie, jeśli istnieje
  const [token, setToken] = useState(() => localStorage.getItem('jwt_token'));
  const [user, setUser] = useState(null);

  useEffect(() => {
    if (token) {
      try {
        // Podstawowe zdekodowanie payloadu JWT zapisanej w formacie Base64Url
        const base64Url = token.split('.')[1];
        const base64 = base64Url.replace(/-/g, '+').replace(/_/g, '/');
        const jsonPayload = decodeURIComponent(
          atob(base64)
            .split('')
            .map((c) => '%' + ('00' + c.charCodeAt(0).toString(16)).slice(-2))
            .join('')
        );
        const decoded = JSON.parse(jsonPayload);
        
        // Wyciągnięcie nowych pól z internalowego tokena JWT
        setUser({ 
          accountId: decoded.sub,
          name: decoded.name || decoded.sub || 'User',
          iat: decoded.iat,
          exp: decoded.exp
        });
      } catch (error) {
        console.error('Błąd podczas dekodowania tokenu JWT:', error);
        logout(); // Token jest zniekształcony, usuwamy go i wylogowujemy usera
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

  const logout = () => {
    localStorage.removeItem('jwt_token');
    setToken(null);
    setUser(null);
  };

  return (
    <AuthContext.Provider value={{ token, user, login, logout, isAuthenticated: !!token || !!user }}>
      {children}
    </AuthContext.Provider>
  );
};

export const useAuth = () => useContext(AuthContext);