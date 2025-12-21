"use client";

import React, { createContext, useContext, useEffect, useState } from "react";
import { authFetch, setToken, getToken } from "@/lib/api";

const AuthContext = createContext(undefined);

export function AuthProvider({ children }) {
  const [user, setUser] = useState(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const token = getToken();
    if (token) {
      setUser({ status: "authenticated" });
    }
    setLoading(false);
  }, []);

  async function login(email, password) {
    const data = await authFetch("/login", {
      method: "POST",
      body: { username: email, password },
    });
    setToken(data.token);
    setUser({ username: email, status: data.status || "authenticated" });
  }

  async function register(email, password) {

    const data = await authFetch("/register", {
      method: "POST",
      body: { username: email, password },
    });
    return data;
  }

  function logout() {
    setToken(null);
    setUser(null);
  }

  const value = { user, loading, login, register, logout };

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}

export function useAuth() {
  const ctx = useContext(AuthContext);
  if (!ctx) throw new Error("useAuth must be used inside <AuthProvider />");
  return ctx;
}
