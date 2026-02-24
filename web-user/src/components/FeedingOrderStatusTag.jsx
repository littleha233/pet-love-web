const MAP = {
  PENDING_PROVIDER_ACCEPT: ["待接单", "feed-status-pending"],
  REJECTED_BY_PROVIDER: ["已拒单", "feed-status-rejected"],
  CONFIRMED: ["已确认", "feed-status-confirmed"],
  IN_SERVICE: ["服务中", "feed-status-active"],
  WAITING_OWNER_CONFIRM: ["待主人确认", "feed-status-waiting"],
  COMPLETED: ["已完成", "feed-status-completed"],
  CANCELLED_BY_OWNER: ["主人取消", "feed-status-cancelled"],
  CANCELLED_BY_PROVIDER: ["服务者取消", "feed-status-cancelled"]
};

function FeedingOrderStatusTag({ status }) {
  const [label, cls] = MAP[status] || [status || "-", "feed-status-default"];
  return <span className={`status-pill ${cls}`}>{label}</span>;
}

export default FeedingOrderStatusTag;
