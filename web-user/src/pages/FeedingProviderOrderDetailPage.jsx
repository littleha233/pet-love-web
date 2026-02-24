import { useEffect, useState } from "react";
import { Link, useParams } from "react-router-dom";
import FeedingOrderStatusTag from "../components/FeedingOrderStatusTag.jsx";
import FeedingVisitStatusTag from "../components/FeedingVisitStatusTag.jsx";
import FeedingVisitEditor from "../components/FeedingVisitEditor.jsx";
import {
  cancelProviderFeedingOrder,
  getFeedingOrderDetail,
  respondProviderFeedingOrder,
  startProviderVisit
} from "../api/feedingApi";
import { formatDateTime } from "../utils/format";

function FeedingProviderOrderDetailPage() {
  const { orderId } = useParams();
  const [detail, setDetail] = useState(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState("");
  const [message, setMessage] = useState("");

  async function loadDetail() {
    setLoading(true);
    setError("");
    try {
      const data = await getFeedingOrderDetail(orderId);
      setDetail(data);
    } catch (err) {
      setError(err.message || "加载订单详情失败");
    } finally {
      setLoading(false);
    }
  }

  useEffect(() => {
    loadDetail();
  }, [orderId]);

  async function onRespond(action) {
    setMessage("");
    try {
      const quotedTotalAmount =
        action === "ACCEPT"
          ? window.prompt("请输入本单报价（元），可留空", detail?.quotedTotalAmount || "")
          : "";
      const providerResponseNote = window.prompt("可选：给主人的说明", "") || undefined;
      await respondProviderFeedingOrder(orderId, {
        action,
        quotedTotalAmount:
          action === "ACCEPT" && quotedTotalAmount !== null && quotedTotalAmount !== ""
            ? Number(quotedTotalAmount)
            : undefined,
        providerResponseNote
      });
      setMessage(action === "ACCEPT" ? "已接单" : "已拒单");
      await loadDetail();
    } catch (err) {
      setMessage(err.message || "操作失败");
    }
  }

  async function onStartVisit(visitId) {
    setMessage("");
    try {
      await startProviderVisit(visitId);
      setMessage("已开始本次服务");
      await loadDetail();
    } catch (err) {
      setMessage(err.message || "开始服务失败");
    }
  }

  async function onCancelOrder() {
    setMessage("");
    try {
      const reason = window.prompt("请输入取消原因", "服务者临时取消");
      if (!reason) {
        return;
      }
      await cancelProviderFeedingOrder(orderId, { reason });
      setMessage("订单已取消");
      await loadDetail();
    } catch (err) {
      setMessage(err.message || "取消失败");
    }
  }

  if (loading) {
    return <p className="helper-text">加载中...</p>;
  }
  if (error) {
    return <p className="error-text">{error}</p>;
  }
  if (!detail) {
    return null;
  }

  return (
    <div className="page-stack">
      <section className="card page-banner">
        <p className="eyebrow">订单详情（服务者）</p>
        <h1>订单号：{detail.orderNo}</h1>
        <div className="status-line">
          <FeedingOrderStatusTag status={detail.status} />
          <span>主人：{detail.ownerNickname}</span>
          <span>联系人：{detail.contactName} / {detail.contactMobileMasked}</span>
        </div>
      </section>

      <section className="detail-layout">
        <article className="card detail-main">
          <h3>服务地址</h3>
          <p>{detail.serviceAddressDetail}</p>
          <p className="helper-text">地址备注：{detail.serviceAddressNote || "-"}</p>
          <p className="helper-text">主人留言：{detail.ownerNote || "-"}</p>
          <p className="helper-text">报价：{detail.quotedTotalAmount ? `¥${detail.quotedTotalAmount}` : "待填写"}</p>

          <h3>Visit 执行</h3>
          <div className="list-stack">
            {(detail.visits || []).map((visit) => (
              <div key={visit.visitId} className="list-card">
                <div className="list-card-main">
                  <div className="status-line">
                    <FeedingVisitStatusTag status={visit.status} />
                    <span>第 {visit.visitIndex} 次</span>
                  </div>
                  <p className="helper-text">
                    计划：{formatDateTime(visit.plannedStartAt)} - {formatDateTime(visit.plannedEndAt)}
                  </p>
                  <p className="helper-text">
                    实际：{formatDateTime(visit.actualStartAt)} - {formatDateTime(visit.actualEndAt)}
                  </p>
                  <div className="detail-image-grid">
                    {(visit.photos || []).map((photo) => (
                      <img key={`${visit.visitId}-${photo.fileId}-${photo.sortOrder}`} src={photo.url} alt="留痕" />
                    ))}
                  </div>

                  {visit.status === "PENDING" ? (
                    <button className="secondary-btn" type="button" onClick={() => onStartVisit(visit.visitId)}>
                      开始本次服务
                    </button>
                  ) : null}

                  {visit.status === "STARTED" ? (
                    <FeedingVisitEditor visit={visit} onSuccess={loadDetail} />
                  ) : null}
                </div>
              </div>
            ))}
          </div>
        </article>

        <aside className="card detail-side">
          <h3>订单操作</h3>
          <div className="action-row">
            {detail.status === "PENDING_PROVIDER_ACCEPT" ? (
              <>
                <button className="primary-btn" type="button" onClick={() => onRespond("ACCEPT")}>接单</button>
                <button className="secondary-btn" type="button" onClick={() => onRespond("REJECT")}>拒单</button>
              </>
            ) : null}
            {(detail.status === "PENDING_PROVIDER_ACCEPT" || detail.status === "CONFIRMED") ? (
              <button className="secondary-btn" type="button" onClick={onCancelOrder}>
                取消订单
              </button>
            ) : null}
          </div>

          {message ? <p className="helper-text">{message}</p> : null}
          <Link className="secondary-btn full-btn" to="/provider/feeding/orders">
            返回我的接单
          </Link>
        </aside>
      </section>
    </div>
  );
}

export default FeedingProviderOrderDetailPage;
