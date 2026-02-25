const MAP = {
  DRAFT: ["草稿", "rescue-guide-status-draft"],
  PUBLISHED: ["已发布", "rescue-guide-status-published"],
  OFFLINE: ["已下线", "rescue-guide-status-offline"]
};

function RescueGuideStatusTag({ status }) {
  const [label, cls] = MAP[status] || [status || "-", "rescue-guide-status-default"];
  return <span className={`status-pill ${cls}`}>{label}</span>;
}

export default RescueGuideStatusTag;
