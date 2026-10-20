import { createContext, useContext, useState, useCallback } from "react";
import client from "../api/client";

const AuthContext = createContext(null);

export function AuthProvider({ children }) {
  const [user, setUser] = useState(() => {
    const stored = localStorage.getItem("skripty_user");
    return stored ? JSON.parse(stored) : null;
  });

  const login = useCallback(async (email, password) => {
    const { data } = await client.post("/auth/login", { email, password });
    persistSession(data);
    return data;
  }, []);

  const register = useCallback(async (email, password, fullName) => {
    const { data } = await client.post("/auth/register", { email, password, fullName });
    persistSession(data);
    return data;
  }, []);

  const logout = useCallback(() => {
    localStorage.removeItem("skripty_token");
    localStorage.removeItem("skripty_user");
    setUser(null);
  }, []);

  function persistSession(data) {
    localStorage.setItem("skripty_token", data.token);
    const userInfo = { email: data.email, fullName: data.fullName };
    localStorage.setItem("skripty_user", JSON.stringify(userInfo));
    setUser(userInfo);
  }

  return (
    <AuthContext.Provider value={{ user, login, register, logout }}>
      {children}
    </AuthContext.Provider>
  );
}

export function useAuth() {
  const ctx = useContext(AuthContext);
  if (!ctx) throw new Error("useAuth must be used within AuthProvider");
  return ctx;
}
