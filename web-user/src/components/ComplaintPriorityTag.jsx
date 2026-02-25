const LABEL_MAP = {
  LOW: "低",
  MEDIUM: "中",
  HIGH: "高",
  URGENT: "紧急"
};

function ComplaintPriorityTag({ priority }) {
  const label = LABEL_MAP[priority] || priority || "-";
  return <span className="soft-tag">优先级：{label}</span>;
}

export default ComplaintPriorityTag;
