"use client";

import Link from "next/link";
import { useAuth } from "./AuthProvider";

export default function Navbar() {
  const { user, logout } = useAuth();

  return (
    <header className="nav-header">
      <div className="nav-inner">
        <Link href="/dashboard" className="nav-logo">
          RoomBooking
        </Link>

        <nav className="nav-links">
          {user && (
            <>
              <Link href="/dashboard">Dashboard</Link>
              <Link href="/bookings">My Bookings</Link>
            </>
          )}

          {!user ? (
            <>
              <Link href="/login" className="btn-outline">
                Login
              </Link>
              <Link href="/register" className="btn-primary">
                Register
              </Link>
            </>
          ) : (
            <button onClick={logout} className="btn-primary">
              Logout
            </button>
          )}
        </nav>
      </div>
    </header>
  );
}
