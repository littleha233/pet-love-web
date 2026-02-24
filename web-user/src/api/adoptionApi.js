import { request } from "./client";

export function listAdoptionPosts(params) {
  return request("/api/v1/adoptions/posts", { query: params });
}

export function getAdoptionPostDetail(postId) {
  return request(`/api/v1/adoptions/posts/${postId}`, { auth: "user" });
}

export function createRehomePost(payload) {
  return request("/api/v1/adoptions/posts", {
    method: "POST",
    body: payload,
    auth: "user"
  });
}

export function listMyRehomePosts(params) {
  return request("/api/v1/adoptions/my/posts", {
    query: params,
    auth: "user"
  });
}

export function resubmitRehomePost(postId) {
  return request(`/api/v1/adoptions/posts/${postId}/resubmit`, {
    method: "POST",
    auth: "user"
  });
}

export function closeRehomePost(postId) {
  return request(`/api/v1/adoptions/posts/${postId}/close`, {
    method: "POST",
    auth: "user"
  });
}

export function submitAdoptionApplication(postId, payload) {
  return request(`/api/v1/adoptions/posts/${postId}/applications`, {
    method: "POST",
    body: payload,
    auth: "user"
  });
}

export function listMyAdoptionApplications(params) {
  return request("/api/v1/adoptions/my/applications", {
    query: params,
    auth: "user"
  });
}

export function listPostApplications(postId, params) {
  return request(`/api/v1/adoptions/posts/${postId}/applications`, {
    query: params,
    auth: "user"
  });
}

export function handleAdoptionApplication(applicationId, payload) {
  return request(`/api/v1/adoptions/applications/${applicationId}/handle`, {
    method: "POST",
    body: payload,
    auth: "user"
  });
}

export function withdrawAdoptionApplication(applicationId) {
  return request(`/api/v1/adoptions/applications/${applicationId}/withdraw`, {
    method: "POST",
    auth: "user"
  });
}

export function uploadPetImage(file) {
  const formData = new FormData();
  formData.append("file", file);
  formData.append("bizType", "PET_MEDIA");

  return request("/api/v1/files/upload", {
    method: "POST",
    body: formData,
    auth: "user",
    isFormData: true,
    headers: {}
  });
}

export function listAdminAdoptionPosts(params) {
  return request("/api/admin/v1/adoptions/posts", {
    query: params,
    auth: "admin"
  });
}

export function getAdminAdoptionPostDetail(postId) {
  return request(`/api/admin/v1/adoptions/posts/${postId}`, {
    auth: "admin"
  });
}

export function approveAdminAdoptionPost(postId, remark) {
  return request(`/api/admin/v1/adoptions/posts/${postId}/approve`, {
    method: "POST",
    body: { remark: remark || undefined },
    auth: "admin"
  });
}

export function rejectAdminAdoptionPost(postId, payload) {
  return request(`/api/admin/v1/adoptions/posts/${postId}/reject`, {
    method: "POST",
    body: payload,
    auth: "admin"
  });
}

export function offlineAdminAdoptionPost(postId, reason) {
  return request(`/api/admin/v1/adoptions/posts/${postId}/offline`, {
    method: "POST",
    body: { reason },
    auth: "admin"
  });
}
