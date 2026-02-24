const STATUS_MAP = {
  SUBMITTED: { label: "待处理", cls: "submitted" },
  ACCEPTED: { label: "已接受", cls: "accepted" },
  REJECTED: { label: "已拒绝", cls: "rejected" },
  WITHDRAWN: { label: "已撤回", cls: "withdrawn" }
};

function ApplicationStatusTag({ status }) {
  const item = STATUS_MAP[status] || { label: status || "未知", cls: "default" };
  return <span className={`status-pill app-status-${item.cls}`}>{item.label}</span>;
}

export default ApplicationStatusTag;
