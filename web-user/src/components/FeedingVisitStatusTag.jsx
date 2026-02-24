const MAP = {
  PENDING: ["待开始", "feed-status-pending"],
  STARTED: ["进行中", "feed-status-active"],
  DONE: ["已完成", "feed-status-completed"],
  CANCELLED: ["已取消", "feed-status-cancelled"]
};

function FeedingVisitStatusTag({ status }) {
  const [label, cls] = MAP[status] || [status || "-", "feed-status-default"];
  return <span className={`status-pill ${cls}`}>{label}</span>;
}

export default FeedingVisitStatusTag;
