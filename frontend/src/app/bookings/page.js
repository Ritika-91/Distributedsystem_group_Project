"use client";

import { useEffect, useState } from "react";
import { useAuth } from "@/components/AuthProvider";
import { bookingFetch } from "@/lib/api";
import Link from "next/link";

export default function BookingsPage() {
  const { user, loading } = useAuth();
  const [bookings, setBookings] = useState([]);
  const [error, setError] = useState(null);
  const [loadingBookings, setLoadingBookings] = useState(false);

  useEffect(() => {
    if (!user) return;

    setLoadingBookings(true);
    bookingFetch("/bookings/me")
      .then((data) => setBookings(data))
      .catch((err) => setError(err.message || "Failed to load bookings"))
      .finally(() => setLoadingBookings(false));
  }, [user]);

  if (loading) return <p>Loading...</p>;

  if (!user) {
    return (
      <div className="page">
        <p>You must be logged in to view your bookings.</p>
        <Link href="/login" className="link">
          Go to login
        </Link>
      </div>
    );
  }

  return (
    <div className="page">
      <h1 className="page-title">My bookings</h1>

      {error && <p className="msg msg-error">{error}</p>}
      {loadingBookings && <p>Loading bookings...</p>}

      {!loadingBookings && bookings.length === 0 && (
        <p className="page-subtitle">
          You have no bookings yet.{" "}
          <Link href="/dashboard" className="link">
            Book a room
          </Link>
          .
        </p>
      )}

      <div className="booking-list">
        {bookings.map((b) => (
          <div key={b.id} className="booking-item">
            <div>
              <p className="room-name">{b.roomName}</p>
              <p className="room-meta">
                {b.roomType} ·{" "}
                {new Date(b.startTime).toLocaleString()} –{" "}
                {new Date(b.endTime).toLocaleString()}
              </p>
            </div>
            <span className="status-pill">{b.status}</span>
          </div>
        ))}
      </div>
    </div>
  );
}
