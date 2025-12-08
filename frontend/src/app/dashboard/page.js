"use client";

import { useAuth } from "@/components/AuthProvider";
import AvailabilitySearch from "@/components/AvailabilitySearch";
import Link from "next/link";

export default function DashboardPage() {
  const { user, loading } = useAuth();

  if (loading) return <p>Loading...</p>;

  if (!user) {
    return (
      <div className="page">
        <p>You must be logged in to view the dashboard.</p>
        <Link href="/login" className="link">
          Go to login
        </Link>
      </div>
    );
  }

  return (
    <div className="page">
      <section className="mb-large">
        <h1 className="page-title">Hi {user.name}, book a room</h1>
        <p className="page-subtitle">
          Choose a date and time to see available rooms. Once you lock a room,
          confirm the booking before the lock expires.
        </p>
      </section>

      <AvailabilitySearch />
    </div>
  );
}
