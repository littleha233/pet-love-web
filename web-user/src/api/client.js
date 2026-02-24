const API_BASE_URL = (import.meta.env.VITE_API_BASE_URL || "").replace(/\/$/, "");

export class ApiError extends Error {
  constructor(message, code, status) {
    super(message);
    this.name = "ApiError";
    this.code = code;
    this.status = status;
  }
}

function resolveToken(auth) {
  if (auth === "user") {
    return window.localStorage.getItem("petlove_user_access_token");
  }
  if (auth === "admin") {
    return window.localStorage.getItem("petlove_admin_access_token");
  }
  return null;
}

function buildUrl(path, query) {
  const normalizedPath = path.startsWith("/") ? path : `/${path}`;
  const url = new URL(`${API_BASE_URL}${normalizedPath}`, window.location.origin);
  if (query) {
    Object.entries(query).forEach(([key, value]) => {
      if (value === undefined || value === null || value === "") {
        return;
      }
      url.searchParams.set(key, String(value));
    });
  }
  return url;
}

export async function request(path, options = {}) {
  const {
    method = "GET",
    query,
    body,
    auth = "none",
    headers = {},
    isFormData = false
  } = options;

  const token = resolveToken(auth);
  const mergedHeaders = {
    Accept: "application/json",
    ...headers
  };

  if (!isFormData) {
    mergedHeaders["Content-Type"] = "application/json";
  }

  if (token) {
    mergedHeaders.Authorization = `Bearer ${token}`;
  }

  const response = await fetch(buildUrl(path, query), {
    method,
    headers: mergedHeaders,
    body:
      body === undefined || body === null
        ? undefined
        : isFormData
          ? body
          : JSON.stringify(body)
  });

  const contentType = response.headers.get("content-type") || "";
  const payload = contentType.includes("application/json") ? await response.json() : null;

  if (!response.ok) {
    const message = payload?.message || `HTTP ${response.status}`;
    throw new ApiError(message, payload?.code, response.status);
  }

  if (!payload) {
    return null;
  }

  if (payload.code !== "OK") {
    throw new ApiError(payload.message || "Request failed", payload.code, response.status);
  }

  return payload.data;
}
