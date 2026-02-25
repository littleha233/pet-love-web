import { request } from "./client";

export function listRescueGuides(params) {
  return request("/api/v1/rescue/guides", { query: params });
}

export function getRescueGuideDetail(guideId) {
  return request(`/api/v1/rescue/guides/${guideId}`);
}

export function listRescueResources(params) {
  return request("/api/v1/rescue/resources", { query: params });
}

export function getRescueResourceDetail(resourceId) {
  return request(`/api/v1/rescue/resources/${resourceId}`);
}

export function submitRescueClue(payload) {
  return request("/api/v1/rescue/clues", {
    method: "POST",
    body: payload,
    auth: "user"
  });
}

export function listMyRescueClues(params) {
  return request("/api/v1/rescue/clues/my", {
    query: params,
    auth: "user"
  });
}

export function getMyRescueClueDetail(clueId) {
  return request(`/api/v1/rescue/clues/${clueId}`, {
    auth: "user"
  });
}

export function uploadRescueCluePhoto(file) {
  const formData = new FormData();
  formData.append("file", file);
  formData.append("bizType", "RESCUE_CLUE");

  return request("/api/v1/files/upload", {
    method: "POST",
    body: formData,
    auth: "user",
    isFormData: true,
    headers: {}
  });
}

export function listAdminRescueGuides(params) {
  return request("/api/admin/v1/rescue/guides", {
    query: params,
    auth: "admin"
  });
}

export function getAdminRescueGuideDetail(guideId) {
  return request(`/api/admin/v1/rescue/guides/${guideId}`, {
    auth: "admin"
  });
}

export function createAdminRescueGuide(payload) {
  return request("/api/admin/v1/rescue/guides", {
    method: "POST",
    body: payload,
    auth: "admin"
  });
}

export function upsertAdminRescueGuide(guideId, payload) {
  return request(`/api/admin/v1/rescue/guides/${guideId}`, {
    method: "PUT",
    body: payload,
    auth: "admin"
  });
}

export function publishAdminRescueGuide(guideId) {
  return request(`/api/admin/v1/rescue/guides/${guideId}/publish`, {
    method: "POST",
    auth: "admin"
  });
}

export function offlineAdminRescueGuide(guideId) {
  return request(`/api/admin/v1/rescue/guides/${guideId}/offline`, {
    method: "POST",
    auth: "admin"
  });
}

export function listAdminRescueResources(params) {
  return request("/api/admin/v1/rescue/resources", {
    query: params,
    auth: "admin"
  });
}

export function getAdminRescueResourceDetail(resourceId) {
  return request(`/api/admin/v1/rescue/resources/${resourceId}`, {
    auth: "admin"
  });
}

export function createAdminRescueResource(payload) {
  return request("/api/admin/v1/rescue/resources", {
    method: "POST",
    body: payload,
    auth: "admin"
  });
}

export function upsertAdminRescueResource(resourceId, payload) {
  return request(`/api/admin/v1/rescue/resources/${resourceId}`, {
    method: "PUT",
    body: payload,
    auth: "admin"
  });
}

export function activateAdminRescueResource(resourceId) {
  return request(`/api/admin/v1/rescue/resources/${resourceId}/activate`, {
    method: "POST",
    auth: "admin"
  });
}

export function pauseAdminRescueResource(resourceId) {
  return request(`/api/admin/v1/rescue/resources/${resourceId}/pause`, {
    method: "POST",
    auth: "admin"
  });
}

export function offlineAdminRescueResource(resourceId) {
  return request(`/api/admin/v1/rescue/resources/${resourceId}/offline`, {
    method: "POST",
    auth: "admin"
  });
}

export function listAdminRescueClues(params) {
  return request("/api/admin/v1/rescue/clues", {
    query: params,
    auth: "admin"
  });
}

export function getAdminRescueClueDetail(clueId) {
  return request(`/api/admin/v1/rescue/clues/${clueId}`, {
    auth: "admin"
  });
}

export function updateAdminRescueClueStatus(clueId, payload) {
  return request(`/api/admin/v1/rescue/clues/${clueId}/status`, {
    method: "POST",
    body: payload,
    auth: "admin"
  });
}
