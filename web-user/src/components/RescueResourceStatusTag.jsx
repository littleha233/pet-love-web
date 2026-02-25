const MAP = {
  DRAFT: ["草稿", "rescue-resource-status-draft"],
  ACTIVE: ["启用", "rescue-resource-status-active"],
  PAUSED: ["暂停", "rescue-resource-status-paused"],
  OFFLINE: ["下线", "rescue-resource-status-offline"]
};

function RescueResourceStatusTag({ status }) {
  const [label, cls] = MAP[status] || [status || "-", "rescue-resource-status-default"];
  return <span className={`status-pill ${cls}`}>{label}</span>;
}

export default RescueResourceStatusTag;
