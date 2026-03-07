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

export function formatPublicText(value) {
  if (value === undefined || value === null) {
    return "";
  }

  const cleaned = String(value)
    .replace(/^\s*\[(demo|测试)\]\s*/i, "")
    .replace(/^\s*(demo|测试)[-_]/i, "")
    .replace(/\b(demo|测试)[-_]/gi, "")
    .trim();

  return cleaned || String(value).trim();
}

export function formatReason(reason) {
  const map = {
    CANNOT_APPLY_OWN_POST: "不能申请自己的帖子",
    POST_NOT_PUBLISHED: "帖子当前不可申请",
    ALREADY_APPLIED: "你已提交过申请"
  };
  return map[reason] || reason || "";
}

export function formatPetType(value) {
  const map = {
    CAT: "猫咪",
    DOG: "狗狗",
    OTHER: "其他"
  };
  return map[value] || value || "-";
}

export function formatPetGender(value) {
  const map = {
    MALE: "公",
    FEMALE: "母",
    UNKNOWN: "未知"
  };
  return map[value] || value || "-";
}

export function formatRescueUrgency(value) {
  const map = {
    LOW: "低",
    MEDIUM: "中",
    HIGH: "高",
    URGENT: "紧急",
    EMERGENCY: "紧急"
  };
  return map[value] || value || "-";
}

export function formatRescueGuideScenario(value) {
  const map = {
    FOUND_STRAY_CAT: "发现流浪猫",
    FOUND_STRAY_DOG: "发现流浪狗",
    INJURED_CAT: "发现受伤猫",
    INJURED_DOG: "发现受伤狗",
    ABANDONED_KITTENS: "幼猫救助",
    ABANDONED_PUPPIES: "幼犬救助",
    EMERGENCY_TRANSPORT: "紧急转运"
  };
  return map[value] || value || "通用场景";
}

export function formatRescueResourceType(value) {
  const map = {
    ANIMAL_HOSPITAL: "动物医院",
    SHELTER: "救助站",
    VOLUNTEER_GROUP: "志愿者组织",
    OFFICIAL_CHANNEL: "官方渠道",
    NGO: "公益组织"
  };
  return map[value] || value || "综合";
}

export function formatComplaintTargetType(value) {
  const map = {
    ADOPTION_POST: "送养信息",
    ADOPTION_APPLICATION: "领养申请",
    RESCUE_RESOURCE: "救助资源",
    USER: "用户行为",
    OTHER: "其他问题"
  };
  return map[value] || value || "未分类";
}

export function formatRescueConditionTag(value) {
  const map = {
    INJURED: "受伤",
    BLEEDING: "出血",
    WEAK: "虚弱",
    TRAPPED: "受困",
    KITTEN: "幼猫",
    PUPPY: "幼犬"
  };
  return map[value] || value || "-";
}

export function formatNumber(value) {
  if (value === undefined || value === null) {
    return "-";
  }
  return new Intl.NumberFormat("zh-CN").format(value);
}
