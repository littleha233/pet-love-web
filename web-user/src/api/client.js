const API_BASE_URL = (import.meta.env.VITE_API_BASE_URL || "").replace(/\/$/, "");

const ERROR_CODE_MESSAGES = {
  INVALID_PARAM: "请求参数不正确，请检查后重试",
  UNAUTHORIZED: "登录状态已失效，请重新登录",
  FORBIDDEN: "你暂无权限执行此操作",
  NOT_FOUND: "请求的资源不存在或已删除",
  INTERNAL_ERROR: "服务开小差了，请稍后重试",
  AUTH_OTP_INVALID: "验证码不正确，请重新输入",
  AUTH_OTP_EXPIRED: "验证码已过期，请重新获取",
  AUTH_OTP_TOO_FREQUENT: "获取验证码过于频繁，请稍后再试",
  AUTH_MOBILE_INVALID: "手机号格式不正确，请输入中国大陆手机号",
  AUTH_SMS_BIZ_TYPE_INVALID: "短信业务类型不支持",
  AUTH_SMS_SEND_TOO_FREQUENT: "发送太频繁，请稍后再试",
  AUTH_SMS_MOBILE_DAILY_LIMIT_EXCEEDED: "该手机号今日发送次数已达上限",
  AUTH_SMS_IP_DAILY_LIMIT_EXCEEDED: "当前网络发送次数已达上限，请稍后重试",
  AUTH_SMS_PROVIDER_SEND_FAILED: "短信发送失败，请稍后重试",
  AUTH_SMS_CAPTCHA_REQUIRED: "请先完成人机验证后再发送",
  AUTH_SMS_CAPTCHA_INVALID: "人机验证失败，请重试",
  AUTH_SMS_CODE_NOT_FOUND: "未找到可用验证码，请先发送验证码",
  AUTH_SMS_CODE_EXPIRED: "验证码已过期，请重新发送",
  AUTH_SMS_CODE_INCORRECT: "验证码不正确，请重新输入",
  AUTH_SMS_CODE_ATTEMPTS_EXCEEDED: "验证码尝试次数过多，请重新发送",
  AUTH_SMS_CODE_ALREADY_USED: "验证码已使用，请重新发送",
  AUTH_SMS_CODE_STATUS_INVALID: "验证码状态无效，请重新发送",
  AUTH_LOGIN_MOBILE_BLOCKED: "当前手机号登录受限，请联系管理员",
  AUTH_SMS_SEND_BLOCKED: "当前手机号发送验证码受限，请联系管理员",
  AUTH_TOKEN_ISSUE_FAILED: "登录失败，请稍后重试",
  AUTH_USER_CREATE_FAILED: "账号创建失败，请稍后重试",
  AUTH_SMS_LOG_WRITE_FAILED: "短信服务异常，请稍后重试",
  AUTH_TOKEN_INVALID: "登录状态已失效，请重新登录",
  AUTH_REFRESH_TOKEN_INVALID: "登录状态已失效，请重新登录",
  USER_DISABLED: "账号已被禁用，请联系管理员",
  USER_BANNED: "账号已被封禁，请联系管理员",
  FILE_TYPE_NOT_ALLOWED: "文件类型不支持，请重新选择",
  FILE_TOO_LARGE: "文件过大，请压缩后重试",
  FILE_NOT_READY: "文件尚未处理完成，请稍后重试",
  VERIFICATION_REAL_NAME_REQUIRED: "请先完成实名认证",
  VERIFICATION_ALREADY_APPROVED: "该认证已通过，无需重复提交",
  VERIFICATION_NOT_FOUND: "未找到认证记录",
  VERIFICATION_STATUS_INVALID: "当前认证状态不允许该操作",
  VERIFICATION_FILE_INVALID: "认证文件无效，请重新上传",
  VERIFICATION_FILE_NOT_OWNED: "认证文件不属于当前账号",
  VERIFICATION_SUBMIT_NOT_ALLOWED: "当前状态暂不可提交认证",
  VERIFICATION_REVIEW_NOT_ALLOWED: "当前状态暂不可审核",
  ADOPTION_REAL_NAME_REQUIRED: "请先完成实名认证再发布或申请领养",
  ADOPTION_POST_NOT_FOUND: "送养帖子不存在或不可见",
  ADOPTION_POST_STATUS_INVALID: "当前帖子状态不允许该操作",
  ADOPTION_POST_NOT_OWNER: "该帖子不属于当前账号",
  ADOPTION_POST_REVIEW_NOT_ALLOWED: "当前帖子状态不可审核",
  ADOPTION_POST_FILE_INVALID: "帖子图片无效，请重新上传",
  ADOPTION_POST_FILE_NOT_OWNED: "帖子图片不属于当前账号",
  ADOPTION_APPLICATION_NOT_FOUND: "领养申请不存在",
  ADOPTION_APPLICATION_DUPLICATE: "你已提交过该帖申请",
  ADOPTION_APPLICATION_NOT_ALLOWED: "当前状态不可提交领养申请",
  ADOPTION_APPLICATION_NOT_OWNER: "该申请不属于当前账号",
  ADOPTION_APPLICATION_HANDLE_NOT_ALLOWED: "当前申请状态不可处理",
  ADOPTION_CANNOT_APPLY_OWN_POST: "不能申请自己发布的帖子",
  FEEDING_REAL_NAME_REQUIRED: "请先完成实名认证再发起喂养订单",
  FEEDING_PROVIDER_VERIFICATION_REQUIRED: "请先完成服务者认证后再进行此操作",
  FEEDING_PROVIDER_PROFILE_NOT_FOUND: "服务者资料不存在",
  FEEDING_PROVIDER_PROFILE_STATUS_INVALID: "服务者资料状态不支持当前操作",
  FEEDING_ORDER_NOT_FOUND: "喂养订单不存在或不可访问",
  FEEDING_ORDER_STATUS_INVALID: "当前订单状态不支持该操作",
  FEEDING_ORDER_NOT_OWNER: "该订单不属于当前账号",
  FEEDING_ORDER_NOT_PROVIDER: "你不是该订单的服务者",
  FEEDING_ORDER_CANNOT_CANCEL: "当前订单不可取消",
  FEEDING_ORDER_CANNOT_CONFIRM: "当前订单不可确认完成",
  FEEDING_ORDER_PET_INVALID: "订单宠物信息不合法，请重新选择",
  FEEDING_ORDER_PET_NOT_OWNED: "所选宠物不属于当前账号",
  FEEDING_VISIT_NOT_FOUND: "服务记录不存在",
  FEEDING_VISIT_STATUS_INVALID: "当前服务记录状态不支持该操作",
  FEEDING_VISIT_NOT_PROVIDER: "你不是该服务记录所属服务者",
  FEEDING_VISIT_FILE_INVALID: "留痕图片无效，请重新上传",
  FEEDING_VISIT_FILE_NOT_OWNED: "留痕图片不属于当前账号",
  FEEDING_REVIEW_ALREADY_EXISTS: "该订单已评价，不能重复提交",
  FEEDING_REVIEW_NOT_ALLOWED: "当前订单状态不可评价",
  RESCUE_GUIDE_NOT_FOUND: "救助指引不存在或已下线",
  RESCUE_GUIDE_STATUS_INVALID: "当前指引状态不允许该操作",
  RESCUE_RESOURCE_NOT_FOUND: "救助资源不存在或已下线",
  RESCUE_RESOURCE_STATUS_INVALID: "当前资源状态不允许该操作",
  RESCUE_CLUE_NOT_FOUND: "救助线索不存在",
  RESCUE_CLUE_NOT_OWNER: "该救助线索不属于当前账号",
  RESCUE_CLUE_STATUS_INVALID: "当前线索状态不允许该操作",
  RESCUE_CLUE_FILE_INVALID: "线索图片无效，请重新上传",
  RESCUE_CLUE_FILE_NOT_OWNED: "线索图片不属于当前账号",
  RESCUE_CLUE_SUGGESTED_RESOURCE_INVALID: "推荐资源无效或不在同城",
  RESCUE_PERMISSION_DENIED: "当前账号暂无救助管理权限",
  COMPLAINT_TICKET_NOT_FOUND: "投诉工单不存在或已删除",
  COMPLAINT_TICKET_NOT_OWNER: "该投诉工单不属于当前账号",
  COMPLAINT_TICKET_STATUS_INVALID: "当前工单状态不支持该操作",
  COMPLAINT_TICKET_FILE_INVALID: "投诉证据图片无效，请重新上传",
  COMPLAINT_TICKET_FILE_NOT_OWNED: "投诉证据图片不属于当前账号",
  RISK_BLACKLIST_BLOCKED: "当前账号因风控限制，暂不可执行此操作",
  RISK_ACTION_NOT_ALLOWED: "当前行为已被风控限制",
  RISK_SCOPE_NOT_ALLOWED: "当前业务范围已被风控限制",
  RISK_CONFIG_INVALID: "风控配置无效，请联系管理员",
  CITY_FEATURE_NOT_OPEN: "当前城市暂未开放此功能",
  CITY_FEATURE_READ_DISABLED: "当前城市暂不支持浏览该功能",
  CITY_FEATURE_WRITE_DISABLED: "当前城市暂不支持提交该操作",
  AUDIT_LOG_NOT_FOUND: "审计日志不存在或已删除"
};

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

function fallbackStatusMessage(status) {
  if (status >= 500) {
    return "服务暂时不可用，请稍后重试";
  }
  if (status === 404) {
    return "请求的资源不存在";
  }
  if (status === 403) {
    return "你暂无权限执行此操作";
  }
  if (status === 401) {
    return "登录状态已失效，请重新登录";
  }
  return "请求失败，请稍后重试";
}

function resolveErrorMessage(code, rawMessage, status) {
  if (code && ERROR_CODE_MESSAGES[code]) {
    return ERROR_CODE_MESSAGES[code];
  }
  if (rawMessage && !rawMessage.startsWith("HTTP ")) {
    return rawMessage;
  }
  return fallbackStatusMessage(status);
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

  let response;
  try {
    response = await fetch(buildUrl(path, query), {
      method,
      headers: mergedHeaders,
      body:
        body === undefined || body === null
          ? undefined
          : isFormData
            ? body
            : JSON.stringify(body)
    });
  } catch (err) {
    throw new ApiError("网络连接失败，请检查服务是否启动", "NETWORK_ERROR", 0);
  }

  const contentType = response.headers.get("content-type") || "";
  const payload = contentType.includes("application/json") ? await response.json() : null;

  if (!response.ok) {
    const code = payload?.code || "HTTP_ERROR";
    const message = resolveErrorMessage(code, payload?.message || `HTTP ${response.status}`, response.status);
    throw new ApiError(message, code, response.status);
  }

  if (!payload) {
    return null;
  }

  if (payload.code !== "OK") {
    throw new ApiError(
      resolveErrorMessage(payload.code, payload.message || "Request failed", response.status),
      payload.code,
      response.status
    );
  }

  return payload.data;
}
