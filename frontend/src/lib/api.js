const API_BASE= process.env.NEXT_PUBLIC_API_BASE_URL || "http://localhost:30080"; 

export function getToken(){
    if (typeof window === "undefined") {
         return null;
    }
 
    return localStorage.getItem("jwt_token");
}

export function setToken(token) {
  if (typeof window === "undefined") return;
  if (token) {
    localStorage.setItem("jwt_token", token);
  } 
  else {
    localStorage.removeItem("jwt_token");
  }
}

export async function apiFetch(path, options = {}) {
  const token = getToken();

  const fetchOptions = {
    method: options.method ? options.method : "GET",
    headers: {
      "Content-Type": "application/json",
    },
    body: options.body ? options.body : null,
  };

  // if user passed headers, merge manually
  if (options.headers) {
    for (const key in options.headers) {
      fetchOptions.headers[key] = options.headers[key];
    }
  }

  // add JWT if it exists
  if (token) {
    fetchOptions.headers["Authorization"] = `Bearer ${token}`;
  }

  const res = await fetch(API_BASE + path, fetchOptions);
   if (!res.ok) {
    let text = "";
    try {
      text = await res.text();
    } catch (e) {}
    throw new Error(text || `Request failed with status ${res.status}`);
  }

  return res.json();
}