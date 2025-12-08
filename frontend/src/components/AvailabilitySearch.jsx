"use client";

import { useState } from "react";
import { apiFetch } from "@/lib/api";

export default function AvailabilitySearch() {
  const [date, setDate] = useState("");
  const [startTime, setStartTime] = useState("09:00");
  const [endTime, setEndTime] = useState("11:00");
  const [rooms, setRooms] = useState([]);
  const [loading, setLoading] = useState(false);
  const [selectedRoom, setSelectedRoom] = useState(null);
  const [lockInfo, setLockInfo] = useState(null);
  const [message, setMessage] = useState(null);
  const [error, setError] = useState(null);

  function buildDateTime(d, t) {
    return `${d}T${t}:00`;
  }

  async function handleSearch(e) {
    e.preventDefault();
    if (!date) {
      setError("Please select a date");
      return;
    }

    setError(null);
    setMessage(null);
    setLoading(true);
    setLockInfo(null);
    setSelectedRoom(null);

    try {
      const start = buildDateTime(date, startTime);
      const end = buildDateTime(date, endTime);

      const data = await apiFetch(
        `/rooms/available?startTime=${encodeURIComponent(
          start
        )}&endTime=${encodeURIComponent(end)}`
      );
      setRooms(data);
      if (data.length === 0) {
        setMessage("No rooms available for that time range.");
      }
    } catch (err) {
      setError(err.message || "Failed to fetch availability");
    } finally {
      setLoading(false);
    }
  }

  async function handleLock(room) {
    if (!date) return;

    setError(null);
    setMessage(null);

    try {
      const start = buildDateTime(date, startTime);
      const end = buildDateTime(date, endTime);

      const data = await apiFetch("/availability/lock", {
        method: "POST",
        body: JSON.stringify({
          roomId: room.id,
          startTime: start,
          endTime: end,
        }),
      });

      setSelectedRoom(room);
      setLockInfo(data);
      setMessage(`Room locked. Lock ID: ${data.lockId}. Confirm before it expires.`);
    } catch (err) {
      setError(err.message || "Failed to lock room");
    }
  }

  async function handleConfirm() {
    if (!lockInfo) return;
    setError(null);
    setMessage(null);

    try {
      await apiFetch("/availability/confirm", {
        method: "POST",
        body: JSON.stringify({ lockId: lockInfo.lockId }),
      });

      setMessage("Booking confirmed! Check your bookings page for details.");
      setRooms([]);
      setSelectedRoom(null);
      setLockInfo(null);
    } catch (err) {
      setError(err.message || "Failed to confirm booking");
    }
  }

  async function handleRelease() {
    if (!lockInfo) return;
    setError(null);
    setMessage(null);

    try {
      await apiFetch("/availability/release", {
        method: "POST",
        body: JSON.stringify({ lockId: lockInfo.lockId }),
      });

      setMessage("Lock released.");
      setSelectedRoom(null);
      setLockInfo(null);
    } catch (err) {
      setError(err.message || "Failed to release lock");
    }
  }

  return (
    <section className="card">
      <h2 className="card-title">Search Available Rooms</h2>

      <form onSubmit={handleSearch} className="grid-form">
        <div className="form-field">
          <label>Date</label>
          <input
            type="date"
            value={date}
            onChange={(e) => setDate(e.target.value)}
            required
          />
        </div>

        <div className="form-field">
          <label>Start Time</label>
          <input
            type="time"
            value={startTime}
            onChange={(e) => setStartTime(e.target.value)}
            required
          />
        </div>

        <div className="form-field">
          <label>End Time</label>
          <input
            type="time"
            value={endTime}
            onChange={(e) => setEndTime(e.target.value)}
            required
          />
        </div>

        <div className="form-field form-field-button">
          <button type="submit" disabled={loading} className="btn-primary full-width">
            {loading ? "Searching..." : "Search"}
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
              <button onClick={() => handleLock(room)} className="btn-secondary">
                Lock
              </button>
            </div>
          ))}
        </div>
      )}

      {lockInfo && selectedRoom && (
        <div className="lock-details">
          <h3>Lock details</h3>
          <p>
            Room: <strong>{selectedRoom.name}</strong>
            <br />
            Lock ID: <code>{lockInfo.lockId}</code>
            <br />
            Expires at:{" "}
            <code>{new Date(lockInfo.expiresAt).toLocaleString()}</code>
          </p>
          <div className="lock-actions">
            <button onClick={handleConfirm} className="btn-primary">
              Confirm booking
            </button>
            <button onClick={handleRelease} className="btn-outline">
              Release lock
            </button>
          </div>
        </div>
      )}
    </section>
  );
}
