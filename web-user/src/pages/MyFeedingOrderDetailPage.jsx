import { useEffect, useState } from "react";
import { Link, useParams } from "react-router-dom";
import FeedingOrderStatusTag from "../components/FeedingOrderStatusTag.jsx";
import FeedingVisitStatusTag from "../components/FeedingVisitStatusTag.jsx";
import FeedingReviewModal from "../components/FeedingReviewModal.jsx";
import {
  cancelFeedingOrder,
  confirmFeedingOrderComplete,
  getFeedingOrderDetail,
  submitFeedingOrderReview
} from "../api/feedingApi";
import { formatDateTime } from "../utils/format";

function MyFeedingOrderDetailPage() {
  const { orderId } = useParams();
  const [detail, setDetail] = useState(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState("");
  const [message, setMessage] = useState("");
  const [reviewOpen, setReviewOpen] = useState(false);
  const [reviewSubmitting, setReviewSubmitting] = useState(false);

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

  async function onCancel() {
    setMessage("");
    try {
      await cancelFeedingOrder(orderId);
      setMessage("订单已取消");
      await loadDetail();
    } catch (err) {
      setMessage(err.message || "取消失败");
    }
  }

  async function onConfirmComplete() {
    setMessage("");
    try {
      const remark = window.prompt("可选：填写确认完成备注", "");
      await confirmFeedingOrderComplete(orderId, {
        remark: remark || undefined
      });
      setMessage("已确认完成");
      await loadDetail();
    } catch (err) {
      setMessage(err.message || "确认失败");
    }
  }

  async function onSubmitReview(payload) {
    setReviewSubmitting(true);
    setMessage("");
    try {
      await submitFeedingOrderReview(orderId, payload);
      setMessage("评价提交成功");
      setReviewOpen(false);
      await loadDetail();
    } catch (err) {
      setMessage(err.message || "评价提交失败");
    } finally {
      setReviewSubmitting(false);
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
        <p className="eyebrow">订单详情（主人）</p>
        <h1>订单号：{detail.orderNo}</h1>
        <div className="status-line">
          <FeedingOrderStatusTag status={detail.status} />
          <span>服务者：{detail.providerDisplayName}</span>
          <span>城市：{detail.serviceCityName}</span>
        </div>
      </section>

      <section className="detail-layout">
        <article className="card detail-main">
          <h3>基础信息</h3>
          <div className="kv-grid">
            <p>联系人：{detail.contactName}</p>
            <p>手机号：{detail.contactMobileMasked}</p>
            <p>地址：{detail.serviceAddressDetail}</p>
            <p>地址备注：{detail.serviceAddressNote || "-"}</p>
            <p>预算：{detail.requestedTotalAmount ? `¥${detail.requestedTotalAmount}` : "-"}</p>
            <p>报价：{detail.quotedTotalAmount ? `¥${detail.quotedTotalAmount}` : "待报价"}</p>
          </div>
          <p>留言：{detail.ownerNote || "-"}</p>

          <h3>宠物快照</h3>
          <div className="list-stack">
            {(detail.pets || []).map((pet) => (
              <div key={pet.petId} className="list-card">
                <div className="list-card-main">
                  <p>{pet.petName || `宠物${pet.petId}`}（{pet.petType}）</p>
                  <p className="helper-text">品种：{pet.breed || "-"} · 年龄：{pet.ageMonths ?? "-"} 月</p>
                  <p className="helper-text">照护要点：{pet.specialCareNote || "-"}</p>
                </div>
              </div>
            ))}
          </div>

          <h3>Visit 留痕</h3>
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
                  <p className="helper-text">完成项：
                    {visit.foodDone ? " 喂食" : ""}
                    {visit.waterDone ? " 换水" : ""}
                    {visit.litterDone ? " 清理" : ""}
                    {visit.playDone ? " 陪玩" : ""}
                    {!visit.foodDone && !visit.waterDone && !visit.litterDone && !visit.playDone ? " -" : ""}
                  </p>
                  <p className="helper-text">观察：{visit.healthObservation || "-"}</p>
                  <p className="helper-text">备注：{visit.visitNote || "-"}</p>
                  <div className="detail-image-grid">
                    {(visit.photos || []).map((photo) => (
                      <img key={`${visit.visitId}-${photo.fileId}-${photo.sortOrder}`} src={photo.url} alt="服务留痕" />
                    ))}
                  </div>
                </div>
              </div>
            ))}
          </div>

          {detail.review ? (
            <>
              <h3>我的评价</h3>
              <p>综合评分：{detail.review.ratingOverall}</p>
              <p className="helper-text">内容：{detail.review.content || "-"}</p>
            </>
          ) : null}
        </article>

        <aside className="card detail-side">
          <h3>订单操作</h3>
          <div className="action-row">
            {detail.viewerContext?.canCancel ? (
              <button className="secondary-btn" type="button" onClick={onCancel}>
                取消订单
              </button>
            ) : null}
            {detail.viewerContext?.canConfirmComplete ? (
              <button className="primary-btn" type="button" onClick={onConfirmComplete}>
                确认完成
              </button>
            ) : null}
            {detail.viewerContext?.canReview ? (
              <button className="primary-btn" type="button" onClick={() => setReviewOpen(true)}>
                提交评价
              </button>
            ) : null}
          </div>

          {message ? <p className="helper-text">{message}</p> : null}
          <Link className="secondary-btn full-btn" to="/me/feeding/orders">
            返回我的订单
          </Link>
        </aside>
      </section>

      <FeedingReviewModal
        open={reviewOpen}
        submitting={reviewSubmitting}
        onClose={() => setReviewOpen(false)}
        onSubmit={onSubmitReview}
      />
    </div>
  );
}

export default MyFeedingOrderDetailPage;
