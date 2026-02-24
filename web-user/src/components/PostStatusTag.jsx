const STATUS_MAP = {
  PENDING_REVIEW: { label: "待审核", cls: "pending" },
  PUBLISHED: { label: "已发布", cls: "published" },
  REJECTED: { label: "已驳回", cls: "rejected" },
  CLOSED: { label: "已关闭", cls: "closed" },
  OFFLINE: { label: "已下架", cls: "offline" }
};

function PostStatusTag({ status }) {
  const item = STATUS_MAP[status] || { label: status || "未知", cls: "default" };
  return <span className={`status-pill post-status-${item.cls}`}>{item.label}</span>;
}

export default PostStatusTag;
