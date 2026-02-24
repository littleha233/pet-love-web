export function formatDateTime(value) {
  if (!value) {
    return "-";
  }
  const date = new Date(value);
  if (Number.isNaN(date.getTime())) {
    return value;
  }
  return date.toLocaleString("zh-CN", {
    year: "numeric",
    month: "2-digit",
    day: "2-digit",
    hour: "2-digit",
    minute: "2-digit"
  });
}

export function formatReason(reason) {
  const map = {
    CANNOT_APPLY_OWN_POST: "不能申请自己的帖子",
    POST_NOT_PUBLISHED: "帖子当前不可申请",
    ALREADY_APPLIED: "你已提交过申请"
  };
  return map[reason] || reason || "";
}

export function formatNumber(value) {
  if (value === undefined || value === null) {
    return "-";
  }
  return new Intl.NumberFormat("zh-CN").format(value);
}
