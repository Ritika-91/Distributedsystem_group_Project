"use client";

import { useState } from "react";
import { bookingFetch } from "@/lib/api";

export default function AvailabilitySearch() {
  const [date, setDate] = useState("");
  const [startTime, setStartTime] = useState("09:00");
  const [endTime, setEndTime] = useState("11:00");

  const [rooms, setRooms] = useState([]);
  const [loading, setLoading] = useState(false);
  const [message, setMessage] = useState(null);
  const [error, setError] = useState(null);

  function buildDateTime(d, t) {
    return `${d}T${t}:00`;
  }

  async function handleSearch(e) {
    e.preventDefault();
    setError(null);
    setMessage(null);

    if (!date) {
      setError("Please select a date");
      return;
    }

    // ✅ simplest: you can hardcode rooms for demo if you don’t have a rooms API
    // Replace with real API later.
    setRooms([
      { id: 1, name: "Room A", type: "Study", capacity: 6, building: "Library" },
      { id: 2, name: "Room B", type: "Meeting", capacity: 10, building: "Engineering" },
    ]);
    setMessage("Select a room to book.");
  }

  async function handleBook(room) {
    setError(null);
    setMessage(null);
    setLoading(true);

    try {
      const start = buildDateTime(date, startTime);
      const end = buildDateTime(date, endTime);

      // ✅ Call Booking Service ONLY
      const created = await bookingFetch("/bookings", {
        method: "POST",
        body: {
          roomId: room.id,
          startTime: start,
          endTime: end,
          checkInDate: date,
          checkOutDate: date,
        },
      });

      setMessage(`Booking created! Status: ${created.status} (Booking ID: ${created.id})`);
      setRooms([]);
    } catch (err) {
      setError(err.message || "Failed to create booking");
    } finally {
      setLoading(false);
    }
  }

  return (
    <section className="card">
      <h2 className="card-title">Search & Book a Room</h2>

      <form onSubmit={handleSearch} className="grid-form">
        <div className="form-field">
          <label>Date</label>
          <input type="date" value={date} onChange={(e) => setDate(e.target.value)} required />
        </div>

        <div className="form-field">
          <label>Start Time</label>
          <input type="time" value={startTime} onChange={(e) => setStartTime(e.target.value)} required />
        </div>

        <div className="form-field">
          <label>End Time</label>
          <input type="time" value={endTime} onChange={(e) => setEndTime(e.target.value)} required />
        </div>

        <div className="form-field form-field-button">
          <button type="submit" disabled={loading} className="btn-primary full-width">
            {loading ? "Working..." : "Search"}
          </button>
        </div>
      </form>

      {error && <p className="msg msg-error">{error}</p>}
      {message && <p className="msg msg-success">{message}</p>}

      {rooms.length > 0 && (
        <div className="room-list">
          {rooms.map((room) => (
            <div key={room.id} className="room-item">
              <div>
                <p className="room-name">{room.name}</p>
                <p className="room-meta">
                  {room.type} · Capacity {room.capacity}
                  {room.building ? ` · ${room.building}` : ""}
                </p>
              </div>
              <button onClick={() => handleBook(room)} className="btn-primary" disabled={loading}>
                Book
              </button>
            </div>
          ))}
        </div>
      )}
    </section>
  );
}
