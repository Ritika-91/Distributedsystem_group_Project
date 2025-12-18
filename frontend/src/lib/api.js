const AUTH_BASE =
  process.env.NEXT_PUBLIC_AUTH_API_BASE_URL || "http://localhost:5000";

const BOOKING_BASE =
  process.env.NEXT_PUBLIC_BOOKING_API_BASE_URL || "http://localhost:8080";

export function getToken() {
  if (typeof window === "undefined") return null;
  return localStorage.getItem("jwt_token");
}

export function setToken(token) {
  if (typeof window === "undefined") return;
  if (token) localStorage.setItem("jwt_token", token);
  else localStorage.removeItem("jwt_token");
}

async function baseFetch(baseUrl, path, options = {}) {
  const token = getToken();
  const method = options.method || "GET";

  const headers = {
    "Content-Type": "application/json",
    ...(options.headers || {}),
  };

  if (token) {
    headers["Authorization"] = `Bearer ${token}`;
  }

  const fetchOptions = {
    method,
    headers,
    ...(options.body !== undefined
      ? {
          body:
            typeof options.body === "string"
              ? options.body
              : JSON.stringify(options.body),
        }
      : {}),
  };

  const res = await fetch(baseUrl + path, fetchOptions);

  if (!res.ok) {
    const text = await res.text().catch(() => "");
    throw new Error(text || `Request failed (${res.status})`);
  }

  const contentType = res.headers.get("content-type") || "";
  return contentType.includes("application/json")
    ? res.json()
    : res.text();
}

/* ---------- PUBLIC HELPERS ---------- */

// Auth service (login / register)
export function authFetch(path, options = {}) {
  return baseFetch(AUTH_BASE, path, options);
}

// Booking service (bookings, availability via booking)
export function bookingFetch(path, options = {}) {
  return baseFetch(BOOKING_BASE, path, options);
}
