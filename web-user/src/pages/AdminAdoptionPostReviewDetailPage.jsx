import { useEffect, useState } from "react";
import { Link, useParams } from "react-router-dom";
import PostStatusTag from "../components/PostStatusTag.jsx";
import {
  approveAdminAdoptionPost,
  getAdminAdoptionPostDetail,
  offlineAdminAdoptionPost,
  rejectAdminAdoptionPost
} from "../api/adoptionApi";
import { formatDateTime } from "../utils/format";

function AdminAdoptionPostReviewDetailPage() {
  const { postId } = useParams();
  const [detail, setDetail] = useState(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState("");
  const [notice, setNotice] = useState("");

  async function loadData() {
    setLoading(true);
    setError("");
    try {
      const data = await getAdminAdoptionPostDetail(postId);
      setDetail(data);
    } catch (err) {
      setError(err.message || "加载失败");
    } finally {
      setLoading(false);
    }
  }

  useEffect(() => {
    loadData();
  }, [postId]);

  async function onApprove() {
    const remark = window.prompt("可选：填写审核备注", "") || undefined;
    try {
      await approveAdminAdoptionPost(postId, remark);
      setNotice("审核通过成功");
      await loadData();
    } catch (err) {
      setNotice(err.message || "审核通过失败");
    }
  }

  async function onReject() {
    const rejectReasonCode = window.prompt(
      "请输入驳回原因码（如 PET_INFO_INCOMPLETE）",
      "PET_INFO_INCOMPLETE"
    );
    const rejectReasonText = window.prompt("请输入驳回说明", "");
    if (!rejectReasonCode || !rejectReasonText) {
      return;
    }
    const remark = window.prompt("可选：审核备注", "") || undefined;

    try {
      await rejectAdminAdoptionPost(postId, { rejectReasonCode, rejectReasonText, remark });
      setNotice("已驳回帖子");
      await loadData();
    } catch (err) {
      setNotice(err.message || "驳回失败");
    }
  }

  async function onOffline() {
    const reason = window.prompt("请输入下架原因", "");
    if (!reason) {
      return;
    }
    try {
      await offlineAdminAdoptionPost(postId, reason);
      setNotice("已下架帖子");
      await loadData();
    } catch (err) {
      setNotice(err.message || "下架失败");
    }
  }

  return (
    <div className="page-stack">
      <section className="card page-banner">
        <p className="eyebrow">Admin 审核详情</p>
        <h1>{detail ? detail.title : `帖子 #${postId}`}</h1>
        <p>
          <Link to="/admin/adoptions/posts">返回审核列表</Link>
        </p>
      </section>

      <section className="card page-form-card">
        {loading ? <p className="helper-text">加载中...</p> : null}
        {error ? <p className="error-text">{error}</p> : null}
        {notice ? <p className="helper-text notice-text">{notice}</p> : null}

        {detail ? (
          <div className="detail-layout-single">
            <div className="tag-row">
              <PostStatusTag status={detail.status} />
              <span className="soft-tag">版本：{detail.submitVersion}</span>
              <span className="soft-tag">申请数：{detail.applicationCount}</span>
            </div>

            <p>
              发布者：{detail.publisherNickname}（#{detail.publisherUserId}） 手机：{detail.publisherMobileMasked || "-"}
            </p>
            <p>
              城市：{detail.cityName}（{detail.cityCode}）{detail.districtName ? ` · ${detail.districtName}` : ""}
            </p>
            <p>
              宠物：{detail.petType || "-"} / {detail.petName || "-"} / {detail.petGender || "-"} / 月龄
              {detail.ageMonths ?? "-"}
            </p>
            <p>
              品种：{detail.breed || "-"} · 体重：{detail.weightKg ?? "-"}kg · 绝育：
              {detail.neuteredStatus || "-"} · 疫苗：{detail.vaccinatedStatus || "-"}
            </p>
            <p>性格标签：{(detail.temperamentTags || []).join(" / ") || "-"}</p>
            <p>健康说明：{detail.healthNote || "-"}</p>
            <p>特殊照护：{detail.specialCareNote || "-"}</p>
            <p>帖子正文：{detail.content}</p>
            <p>
              审核人：{detail.reviewedByAdminName || "-"} · 审核时间：{formatDateTime(detail.reviewedAt)}
            </p>
            <p>
              发布时间：{formatDateTime(detail.publishedAt)} · 关闭时间：{formatDateTime(detail.closedAt)}
            </p>
            <p>
              驳回原因：{detail.rejectReasonCode || "-"} {detail.rejectReasonText ? ` / ${detail.rejectReasonText}` : ""}
            </p>

            <div className="detail-image-grid">
              {(detail.petImages || []).map((url) => (
                <img key={url} src={url} alt="宠物图" />
              ))}
            </div>

            <div className="action-row">
              {detail.status === "PENDING_REVIEW" ? (
                <>
                  <button className="primary-btn" type="button" onClick={onApprove}>
                    审核通过
                  </button>
                  <button className="secondary-btn" type="button" onClick={onReject}>
                    驳回
                  </button>
                </>
              ) : null}
              {["PUBLISHED", "CLOSED", "REJECTED"].includes(detail.status) ? (
                <button className="secondary-btn" type="button" onClick={onOffline}>
                  强制下架
                </button>
              ) : null}
            </div>
          </div>
        ) : null}
      </section>
    </div>
  );
}

export default AdminAdoptionPostReviewDetailPage;
