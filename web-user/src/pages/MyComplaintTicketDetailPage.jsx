import { useEffect, useMemo, useState } from "react";
import { Link, useParams } from "react-router-dom";
import ComplaintStatusTag from "../components/ComplaintStatusTag.jsx";
import ComplaintPriorityTag from "../components/ComplaintPriorityTag.jsx";
import {
  cancelComplaintTicket,
  getMyComplaintTicketDetail,
  replyComplaintTicket
} from "../api/opsApi";
import { formatDateTime } from "../utils/format";

function MyComplaintTicketDetailPage() {
  const { ticketId } = useParams();
  const [detail, setDetail] = useState(null);
  const [loading, setLoading] = useState(false);
  const [notice, setNotice] = useState("");
  const [replyContent, setReplyContent] = useState("");
  const [replying, setReplying] = useState(false);
  const [cancelling, setCancelling] = useState(false);

  useEffect(() => {
    let cancelled = false;

    async function loadDetail() {
      setLoading(true);
      setNotice("");
      try {
        const data = await getMyComplaintTicketDetail(ticketId);
        if (!cancelled) {
          setDetail(data);
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

  const canReply = useMemo(() => {
    if (!detail?.status) {
      return false;
    }
    return ["SUBMITTED", "IN_REVIEW", "WAITING_USER"].includes(detail.status);
  }, [detail]);

  const canCancel = useMemo(() => {
    if (!detail?.status) {
      return false;
    }
    return ["SUBMITTED", "WAITING_USER"].includes(detail.status);
  }, [detail]);

  async function reloadDetail() {
    const data = await getMyComplaintTicketDetail(ticketId);
    setDetail(data);
  }

  async function onReply(event) {
    event.preventDefault();
    if (!replyContent.trim()) {
      setNotice("请输入补充说明内容");
      return;
    }

    setReplying(true);
    setNotice("");
    try {
      await replyComplaintTicket(ticketId, { content: replyContent.trim() });
      setReplyContent("");
      setNotice("补充说明已提交");
      await reloadDetail();
    } catch (err) {
      setNotice(err.message || "提交补充说明失败");
    } finally {
      setReplying(false);
    }
  }

  async function onCancel() {
    if (!window.confirm("确认取消该投诉工单吗？")) {
      return;
    }

    setCancelling(true);
    setNotice("");
    try {
      await cancelComplaintTicket(ticketId);
      setNotice("工单已取消");
      await reloadDetail();
    } catch (err) {
      setNotice(err.message || "取消工单失败");
    } finally {
      setCancelling(false);
    }
  }

  return (
    <div className="page-stack">
      <section className="card page-banner">
        <p className="eyebrow">帮助与投诉</p>
        <h1>{detail ? `工单 #${detail.ticketNo}` : `工单 #${ticketId}`}</h1>
        <p>
          <Link to="/me/support/complaints">返回我的投诉列表</Link>
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
            <p>内容：{detail.content}</p>
            <p>对象 ID：{detail.targetId || "-"}</p>
            <p>联系手机号：{detail.contactMobileMasked || "-"}</p>
            <p>分流备注：{detail.triageNote || "-"}</p>
            <p>处理备注：{detail.resolutionNote || "-"}</p>
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
                    {reply.authorName || "系统"} · {formatDateTime(reply.createdAt)}
                  </span>
                  <p>{reply.content}</p>
                </li>
              ))}
            </ul>

            {canReply ? (
              <form className="stack-form" onSubmit={onReply}>
                <label>
                  补充说明
                  <textarea
                    value={replyContent}
                    onChange={(event) => setReplyContent(event.target.value)}
                    rows={4}
                    maxLength={2000}
                    required
                  />
                </label>
                <button className="primary-btn" type="submit" disabled={replying}>
                  {replying ? "提交中..." : "提交补充说明"}
                </button>
              </form>
            ) : null}

            {canCancel ? (
              <button className="secondary-btn" type="button" disabled={cancelling} onClick={onCancel}>
                {cancelling ? "取消中..." : "取消工单"}
              </button>
            ) : null}
          </div>
        ) : null}
      </section>
    </div>
  );
}

export default MyComplaintTicketDetailPage;
