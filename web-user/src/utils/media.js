const API_BASE_URL = (import.meta.env.VITE_API_BASE_URL || "").replace(/\/$/, "");

export function resolveMediaUrl(rawUrl) {
  if (!rawUrl) {
    return "";
  }

  const url = String(rawUrl).trim();
  if (!url) {
    return "";
  }

  if (/^(https?:)?\/\//i.test(url) || url.startsWith("data:") || url.startsWith("blob:")) {
    return url;
  }

  const base = API_BASE_URL || window.location.origin;
  if (url.startsWith("/")) {
    return `${base}${url}`;
  }

  return `${base}/${url}`;
}
