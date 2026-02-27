import { request } from "./client";

export function sendMobileSmsCode(payload) {
  return request("/api/v1/auth/sms/send", {
    method: "POST",
    body: payload
  });
}

export function mobileCodeLogin(payload) {
  return request("/api/v1/auth/login/mobile", {
    method: "POST",
    body: payload
  });
}
