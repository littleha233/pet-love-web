const MAP = {
  SUBMITTED: ["已提交", "rescue-status-submitted"],
  TRIAGED: ["已分流", "rescue-status-triaged"],
  IN_PROGRESS: ["跟进中", "rescue-status-progress"],
  RESOLVED: ["已解决", "rescue-status-resolved"],
  CLOSED: ["已关闭", "rescue-status-closed"],
  INVALID: ["无效线索", "rescue-status-invalid"]
};

function RescueClueStatusTag({ status }) {
  const [label, cls] = MAP[status] || [status || "-", "rescue-status-default"];
  return <span className={`status-pill ${cls}`}>{label}</span>;
}

export default RescueClueStatusTag;
