import React, { createContext, useState, useContext } from 'react';

export const AuthContext = createContext({
  user: null, // { name: string, role: 'user' | 'admin' | null }
  login: (role) => {},
  logout: () => {},
});

export const useAuth = () => useContext(AuthContext);

export const AuthProvider = ({ children }) => {
  const [user, setUser] = useState(null);

  const login = (role) => {
    setUser({ name: role === 'admin' ? 'Admin User' : 'Regular User', role });
  };

  const logout = () => {
    setUser(null);
  };

  return (
    <AuthContext.Provider value={{ user, login, logout }}>
      {children}
    </AuthContext.Provider>
  );
};
