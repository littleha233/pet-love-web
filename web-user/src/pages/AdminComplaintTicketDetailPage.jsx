import { useEffect, useState } from "react";
import { Link, useParams } from "react-router-dom";
import ComplaintPriorityTag from "../components/ComplaintPriorityTag.jsx";
import ComplaintStatusTag from "../components/ComplaintStatusTag.jsx";
import {
  getAdminComplaintTicketDetail,
  replyAdminComplaintTicket,
  updateAdminComplaintTicketStatus
} from "../api/opsApi";
import { formatDateTime } from "../utils/format";

function AdminComplaintTicketDetailPage() {
  const { ticketId } = useParams();
  const [detail, setDetail] = useState(null);
  const [loading, setLoading] = useState(false);
  const [notice, setNotice] = useState("");
  const [replying, setReplying] = useState(false);
  const [updatingStatus, setUpdatingStatus] = useState(false);
  const [replyForm, setReplyForm] = useState({ content: "", isInternalNote: false, moveToStatus: "" });
  const [statusForm, setStatusForm] = useState({
    status: "IN_REVIEW",
    assignedAdminId: "",
    triageNote: "",
    resolutionNote: ""
  });

  useEffect(() => {
    let cancelled = false;

    async function loadDetail() {
      setLoading(true);
      setNotice("");
      try {
        const data = await getAdminComplaintTicketDetail(ticketId);
        if (!cancelled) {
          setDetail(data);
          setStatusForm({
            status: data.status === "SUBMITTED" ? "IN_REVIEW" : data.status || "IN_REVIEW",
            assignedAdminId: data.assignedAdminId ? String(data.assignedAdminId) : "",
            triageNote: data.triageNote || "",
            resolutionNote: data.resolutionNote || ""
          });
        }
      } catch (err) {
        if (!cancelled) {
          setNotice(err.message || "加载工单详情失败");
        }
      } finally {
        if (!cancelled) {
          setLoading(false);
        }
      }
    }

    loadDetail();
    return () => {
      cancelled = true;
    };
  }, [ticketId]);

  async function reloadDetail() {
    const data = await getAdminComplaintTicketDetail(ticketId);
    setDetail(data);
  }

  function onReplyFormChange(event) {
    const { name, value, type, checked } = event.target;
    setReplyForm((prev) => ({ ...prev, [name]: type === "checkbox" ? checked : value }));
  }

  function onStatusFormChange(event) {
    const { name, value } = event.target;
    setStatusForm((prev) => ({ ...prev, [name]: value }));
  }

  async function onSubmitReply(event) {
    event.preventDefault();
    if (!replyForm.content.trim()) {
      setNotice("请输入回复内容");
      return;
    }

    setReplying(true);
    setNotice("");
    try {
      await replyAdminComplaintTicket(ticketId, {
        content: replyForm.content.trim(),
        isInternalNote: replyForm.isInternalNote,
        moveToStatus: replyForm.moveToStatus || undefined
      });
      setReplyForm({ content: "", isInternalNote: false, moveToStatus: "" });
      setNotice("回复成功");
      await reloadDetail();
    } catch (err) {
      setNotice(err.message || "回复失败");
    } finally {
      setReplying(false);
    }
  }

  async function onSubmitStatus(event) {
    event.preventDefault();
    setUpdatingStatus(true);
    setNotice("");

    try {
      await updateAdminComplaintTicketStatus(ticketId, {
        status: statusForm.status,
        assignedAdminId: statusForm.assignedAdminId.trim() ? Number(statusForm.assignedAdminId.trim()) : undefined,
        triageNote: statusForm.triageNote.trim() || undefined,
        resolutionNote: statusForm.resolutionNote.trim() || undefined
      });
      setNotice("状态更新成功");
      await reloadDetail();
    } catch (err) {
      setNotice(err.message || "状态更新失败");
    } finally {
      setUpdatingStatus(false);
    }
  }

  return (
    <div className="page-stack">
      <section className="card page-banner">
        <p className="eyebrow">Admin Ops</p>
        <h1>{detail ? `工单 #${detail.ticketNo}` : `工单 #${ticketId}`}</h1>
        <p>
          <Link to="/admin/ops/complaints">返回工单列表</Link>
        </p>
      </section>

      <section className="card page-form-card">
        {loading ? <p className="helper-text">加载中...</p> : null}
        {notice ? <p className="helper-text notice-text">{notice}</p> : null}

        {detail ? (
          <div className="detail-layout-single">
            <div className="tag-row">
              <ComplaintStatusTag status={detail.status} />
              <ComplaintPriorityTag priority={detail.priority} />
              <span className="soft-tag">对象：{detail.targetType}</span>
            </div>

            <p>标题：{detail.title}</p>
            <p>用户：{detail.reporterUserId}（{detail.reporterNickname || "-"}）</p>
            <p>对象 ID：{detail.targetId || "-"}</p>
            <p>内容：{detail.content}</p>
            <p>联系电话：{detail.contactMobile || "-"}</p>
            <p>当前处理人：{detail.assignedAdminName || "-"}</p>
            <p>分流备注：{detail.triageNote || "-"}</p>
            <p>结案备注：{detail.resolutionNote || "-"}</p>
            <p>
              最后回复：{formatDateTime(detail.lastReplyAt)} · 处理时间：{formatDateTime(detail.handledAt)}
            </p>
            <p>
              创建时间：{formatDateTime(detail.createdAt)} · 更新时间：{formatDateTime(detail.updatedAt)}
            </p>

            <h3>证据图片</h3>
            <div className="detail-image-grid">
              {(detail.evidencePhotos || []).map((photo) => (
                <img key={photo.fileId} src={photo.url} alt="证据图片" />
              ))}
            </div>

            <h3>回复记录</h3>
            <ul className="timeline">
              {(detail.replies || []).map((reply) => (
                <li key={reply.replyId}>
                  <span>
                    {reply.authorName || "系统"} · {reply.authorType}
                    {reply.isInternalNote ? " · INTERNAL" : ""} · {formatDateTime(reply.createdAt)}
                  </span>
                  <p>{reply.content}</p>
                </li>
              ))}
            </ul>

            <form className="stack-form" onSubmit={onSubmitReply}>
              <h3>新增回复</h3>
              <label>
                回复内容
                <textarea
                  name="content"
                  value={replyForm.content}
                  onChange={onReplyFormChange}
                  rows={4}
                  maxLength={2000}
                  required
                />
              </label>
              <label>
                <input
                  type="checkbox"
                  name="isInternalNote"
                  checked={replyForm.isInternalNote}
                  onChange={onReplyFormChange}
                />
                仅后台可见（internal note）
              </label>
              <label>
                回复后切换状态（可选）
                <select name="moveToStatus" value={replyForm.moveToStatus} onChange={onReplyFormChange}>
                  <option value="">不调整</option>
                  <option value="IN_REVIEW">IN_REVIEW</option>
                  <option value="WAITING_USER">WAITING_USER</option>
                </select>
              </label>
              <button className="primary-btn" type="submit" disabled={replying}>
                {replying ? "提交中..." : "提交回复"}
              </button>
            </form>

            <form className="stack-form" onSubmit={onSubmitStatus}>
              <h3>更新状态</h3>
              <label>
                目标状态
                <select name="status" value={statusForm.status} onChange={onStatusFormChange} required>
                  <option value="IN_REVIEW">IN_REVIEW</option>
                  <option value="WAITING_USER">WAITING_USER</option>
                  <option value="RESOLVED">RESOLVED</option>
                  <option value="REJECTED">REJECTED</option>
                  <option value="CLOSED">CLOSED</option>
                </select>
              </label>
              <label>
                分配处理人 ID（可选）
                <input
                  name="assignedAdminId"
                  value={statusForm.assignedAdminId}
                  onChange={onStatusFormChange}
                  placeholder="如 1"
                />
              </label>
              <label>
                分流备注
                <input name="triageNote" value={statusForm.triageNote} onChange={onStatusFormChange} maxLength={255} />
              </label>
              <label>
                结案备注
                <input
                  name="resolutionNote"
                  value={statusForm.resolutionNote}
                  onChange={onStatusFormChange}
                  maxLength={255}
                />
              </label>
              <button className="primary-btn" type="submit" disabled={updatingStatus}>
                {updatingStatus ? "更新中..." : "更新状态"}
              </button>
            </form>
          </div>
        ) : null}
      </section>
    </div>
  );
}

export default AdminComplaintTicketDetailPage;
