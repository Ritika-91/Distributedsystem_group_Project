"use client";

import React, { createContext, useContext, useEffect, useState } from "react";
import { authFetch, setToken, getToken } from "@/lib/api";

const AuthContext = createContext(undefined);

export function AuthProvider({ children }) {
  const [user, setUser] = useState(null);
  const [loading, setLoading] = useState(true);

  // Optional: your Flask service does NOT have /me right now,
  // so we simply consider "token present = logged in".
  useEffect(() => {
    const token = getToken();
    if (token) {
      // minimal user object for UI
      setUser({ status: "authenticated" });
    }
    setLoading(false);
  }, []);

  async function login(email, password) {
    // Flask expects {username, password}
    const data = await authFetch("/login", {
      method: "POST",
      body: { username: email, password },
    });

    // Your Flask response: { message, token, status }
    setToken(data.token);
    setUser({ username: email, status: data.status || "authenticated" });
  }

  async function register(email, password) {
    // Flask expects {username, password} (role optional)
    const data = await authFetch("/register", {
      method: "POST",
      body: { username: email, password },
    });

    // Register endpoint returns message only in your Flask code.
    // So after register, either auto-login or just return.
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
