const LABEL_MAP = {
  SUBMITTED: "已提交",
  IN_REVIEW: "处理中",
  WAITING_USER: "待补充",
  RESOLVED: "已解决",
  REJECTED: "已驳回",
  CLOSED: "已关闭",
  CANCELLED_BY_USER: "已取消"
};

function ComplaintStatusTag({ status }) {
  const label = LABEL_MAP[status] || status || "未知";
  return <span className="soft-tag">{label}</span>;
}

export default ComplaintStatusTag;
