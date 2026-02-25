import { request } from "./client";

export function uploadComplaintEvidence(file) {
  const formData = new FormData();
  formData.append("file", file);
  formData.append("bizType", "COMPLAINT_EVIDENCE");

  return request("/api/v1/files/upload", {
    method: "POST",
    body: formData,
    auth: "user",
    isFormData: true,
    headers: {}
  });
}

export function submitComplaintTicket(payload) {
  return request("/api/v1/support/complaints", {
    method: "POST",
    body: payload,
    auth: "user"
  });
}

export function listMyComplaintTickets(params) {
  return request("/api/v1/support/complaints/my", {
    query: params,
    auth: "user"
  });
}

export function getMyComplaintTicketDetail(ticketId) {
  return request(`/api/v1/support/complaints/${ticketId}`, {
    auth: "user"
  });
}

export function replyComplaintTicket(ticketId, payload) {
  return request(`/api/v1/support/complaints/${ticketId}/reply`, {
    method: "POST",
    body: payload,
    auth: "user"
  });
}

export function cancelComplaintTicket(ticketId) {
  return request(`/api/v1/support/complaints/${ticketId}/cancel`, {
    method: "POST",
    auth: "user"
  });
}

export function listAdminComplaintTickets(params) {
  return request("/api/admin/v1/ops/complaints", {
    query: params,
    auth: "admin"
  });
}

export function getAdminComplaintTicketDetail(ticketId) {
  return request(`/api/admin/v1/ops/complaints/${ticketId}`, {
    auth: "admin"
  });
}

export function replyAdminComplaintTicket(ticketId, payload) {
  return request(`/api/admin/v1/ops/complaints/${ticketId}/reply`, {
    method: "POST",
    body: payload,
    auth: "admin"
  });
}

export function updateAdminComplaintTicketStatus(ticketId, payload) {
  return request(`/api/admin/v1/ops/complaints/${ticketId}/status`, {
    method: "POST",
    body: payload,
    auth: "admin"
  });
}

export function listAdminBlacklists(params) {
  return request("/api/admin/v1/ops/risk/blacklists", {
    query: params,
    auth: "admin"
  });
}

export function upsertAdminBlacklist(blacklistId, payload) {
  return request(`/api/admin/v1/ops/risk/blacklists/${blacklistId}`, {
    method: "PUT",
    body: payload,
    auth: "admin"
  });
}

export function updateAdminBlacklistStatus(blacklistId, payload) {
  return request(`/api/admin/v1/ops/risk/blacklists/${blacklistId}/status`, {
    method: "POST",
    body: payload,
    auth: "admin"
  });
}

export function listAdminCityFeatures(params) {
  return request("/api/admin/v1/ops/city-features", {
    query: params,
    auth: "admin"
  });
}

export function upsertAdminCityFeature(switchId, payload) {
  return request(`/api/admin/v1/ops/city-features/${switchId}`, {
    method: "PUT",
    body: payload,
    auth: "admin"
  });
}

export function getAdminOpsDashboardSummary() {
  return request("/api/admin/v1/ops/dashboard/summary", {
    auth: "admin"
  });
}

export function getAdminOpsDashboardTodos() {
  return request("/api/admin/v1/ops/dashboard/todos", {
    auth: "admin"
  });
}

export function getAdminOpsDashboardTrends(params) {
  return request("/api/admin/v1/ops/dashboard/trends", {
    query: params,
    auth: "admin"
  });
}

export function listAdminOpsAuditLogs(params) {
  return request("/api/admin/v1/ops/audit-logs", {
    query: params,
    auth: "admin"
  });
}

export function getAdminOpsAuditLogDetail(auditLogId) {
  return request(`/api/admin/v1/ops/audit-logs/${auditLogId}`, {
    auth: "admin"
  });
}
