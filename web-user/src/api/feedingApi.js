import { request } from "./client";

export function listFeedingProviders(params) {
  return request("/api/v1/feeding/providers", { query: params });
}

export function getFeedingProviderDetail(providerUserId) {
  return request(`/api/v1/feeding/providers/${providerUserId}`);
}

export function getMyFeedingProviderProfile() {
  return request("/api/v1/feeding/providers/me/profile", { auth: "user" });
}

export function upsertMyFeedingProviderProfile(payload) {
  return request("/api/v1/feeding/providers/me/profile", {
    method: "PUT",
    body: payload,
    auth: "user"
  });
}

export function createFeedingOrder(payload) {
  return request("/api/v1/feeding/orders", {
    method: "POST",
    body: payload,
    auth: "user"
  });
}

export function listMyFeedingPets() {
  return request("/api/v1/feeding/orders/pets/options", {
    auth: "user"
  });
}

export function listMyFeedingOrders(params) {
  return request("/api/v1/feeding/orders/my", {
    query: params,
    auth: "user"
  });
}

export function getFeedingOrderDetail(orderId) {
  return request(`/api/v1/feeding/orders/${orderId}`, {
    auth: "user"
  });
}

export function cancelFeedingOrder(orderId) {
  return request(`/api/v1/feeding/orders/${orderId}/cancel`, {
    method: "POST",
    auth: "user"
  });
}

export function confirmFeedingOrderComplete(orderId, payload) {
  return request(`/api/v1/feeding/orders/${orderId}/confirm-complete`, {
    method: "POST",
    body: payload || {},
    auth: "user"
  });
}

export function submitFeedingOrderReview(orderId, payload) {
  return request(`/api/v1/feeding/orders/${orderId}/review`, {
    method: "POST",
    body: payload,
    auth: "user"
  });
}

export function listProviderFeedingOrders(params) {
  return request("/api/v1/feeding/provider-orders/my", {
    query: params,
    auth: "user"
  });
}

export function respondProviderFeedingOrder(orderId, payload) {
  return request(`/api/v1/feeding/provider-orders/${orderId}/respond`, {
    method: "POST",
    body: payload,
    auth: "user"
  });
}

export function startProviderVisit(visitId) {
  return request(`/api/v1/feeding/provider-orders/visits/${visitId}/start`, {
    method: "POST",
    auth: "user"
  });
}

export function submitProviderVisitLog(visitId, payload) {
  return request(`/api/v1/feeding/provider-orders/visits/${visitId}/submit-log`, {
    method: "POST",
    body: payload,
    auth: "user"
  });
}

export function cancelProviderFeedingOrder(orderId, payload) {
  return request(`/api/v1/feeding/provider-orders/${orderId}/cancel`, {
    method: "POST",
    body: payload,
    auth: "user"
  });
}

export function uploadFeedingLogImage(file) {
  const formData = new FormData();
  formData.append("file", file);
  formData.append("bizType", "FEEDING_LOG");

  return request("/api/v1/files/upload", {
    method: "POST",
    body: formData,
    auth: "user",
    isFormData: true,
    headers: {}
  });
}
