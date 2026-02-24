import { useEffect, useState } from "react";
import { useParams } from "react-router-dom";
import ApplicationStatusTag from "../components/ApplicationStatusTag.jsx";
import PostStatusTag from "../components/PostStatusTag.jsx";
import {
  getAdoptionPostDetail,
  submitAdoptionApplication
} from "../api/adoptionApi";
import { formatDateTime, formatNumber, formatReason } from "../utils/format";

const EMPTY_FORM = {
  message: "",
  livingEnvNote: "",
  petExperienceNote: ""
};

function AdoptionDetailPage() {
  const { postId } = useParams();
  const [detail, setDetail] = useState(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState("");
  const [showApplyForm, setShowApplyForm] = useState(false);
  const [submitting, setSubmitting] = useState(false);
  const [form, setForm] = useState(EMPTY_FORM);
  const [submitMessage, setSubmitMessage] = useState("");

  async function loadDetail() {
    setLoading(true);
    setError("");
    try {
      const data = await getAdoptionPostDetail(postId);
      setDetail(data);
    } catch (err) {
      setError(err.message || "加载详情失败");
    } finally {
      setLoading(false);
    }
  }

  useEffect(() => {
    loadDetail();
  }, [postId]);

  function onFormChange(event) {
    const { name, value } = event.target;
    setForm((prev) => ({ ...prev, [name]: value }));
  }

  async function onSubmitApplication(event) {
    event.preventDefault();
    setSubmitting(true);
    setSubmitMessage("");
    try {
      const created = await submitAdoptionApplication(postId, form);
      setSubmitMessage(`申请已提交：#${created.applicationId}`);
      setForm(EMPTY_FORM);
      setShowApplyForm(false);
      await loadDetail();
    } catch (err) {
      setSubmitMessage(err.message || "提交申请失败");
    } finally {
      setSubmitting(false);
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

  const media = detail.pet?.media || [];
  const viewerContext = detail.viewerContext;

  return (
    <div className="page-stack">
      <section className="card page-banner">
        <p className="eyebrow">送养帖详情</p>
        <h1>{detail.title}</h1>
        <p>{detail.cityName}{detail.districtName ? ` · ${detail.districtName}` : ""}</p>
      </section>

      <section className="detail-layout">
        <div className="card detail-main">
          <div className="detail-image-grid">
            {media.length > 0 ? (
              media.map((item) => (
                <img key={`${item.fileId}-${item.sortOrder}`} src={item.url} alt="宠物照片" />
              ))
            ) : (
              <div className="image-placeholder">暂无图片</div>
            )}
          </div>

          <div className="detail-content">
            <div className="status-line">
              <PostStatusTag status={detail.status} />
              <span>发布时间：{formatDateTime(detail.publishedAt)}</span>
              <span>浏览：{formatNumber(detail.viewCount)}</span>
            </div>

            <h3>帖子内容</h3>
            <p>{detail.content}</p>

            <h3>宠物信息</h3>
            <div className="kv-grid">
              <p>类型：{detail.pet?.petType || "-"}</p>
              <p>昵称：{detail.pet?.name || "-"}</p>
              <p>性别：{detail.pet?.gender || "-"}</p>
              <p>年龄（月）：{detail.pet?.ageMonths ?? "-"}</p>
              <p>品种：{detail.pet?.breed || "-"}</p>
              <p>体重：{detail.pet?.weightKg ?? "-"}</p>
              <p>绝育：{detail.pet?.neuteredStatus || "-"}</p>
              <p>疫苗：{detail.pet?.vaccinatedStatus || "-"}</p>
            </div>
            <p>健康说明：{detail.pet?.healthNote || "-"}</p>
            <p>特殊照护：{detail.pet?.specialCareNote || "-"}</p>

            <div className="tag-row">
              {(detail.pet?.temperamentTags || []).map((tag) => (
                <span key={tag} className="soft-tag">
                  {tag}
                </span>
              ))}
            </div>
          </div>
        </div>

        <aside className="card detail-side">
          <h3>发布者</h3>
          <p>{detail.publisher?.nickname || `用户${detail.publisher?.userId}`}</p>
          <p className="helper-text">实名：{detail.publisher?.isRealNameVerified ? "已实名" : "未实名"}</p>

          <h3>申请统计</h3>
          <p>总申请：{formatNumber(detail.applicationStats?.total || 0)}</p>
          <p>已接受：{formatNumber(detail.applicationStats?.acceptedCount || 0)}</p>

          {viewerContext ? (
            <div className="viewer-context-box">
              <h3>我的状态</h3>
              {viewerContext.hasApplied ? <ApplicationStatusTag status="SUBMITTED" /> : null}
              {!viewerContext.canApply ? (
                <p className="helper-text">{formatReason(viewerContext.cannotApplyReason)}</p>
              ) : (
                <p className="helper-text">可提交领养申请</p>
              )}
            </div>
          ) : (
            <p className="helper-text">登录后可查看申请状态并提交申请。</p>
          )}

          {viewerContext?.canApply ? (
            <button className="primary-btn full-btn" type="button" onClick={() => setShowApplyForm((v) => !v)}>
              {showApplyForm ? "收起申请表" : "提交领养申请"}
            </button>
          ) : null}

          {showApplyForm ? (
            <form className="stack-form" onSubmit={onSubmitApplication}>
              <label>
                申请留言*
                <textarea
                  name="message"
                  value={form.message}
                  onChange={onFormChange}
                  maxLength={2000}
                  rows={4}
                  required
                />
              </label>
              <label>
                居住环境
                <textarea
                  name="livingEnvNote"
                  value={form.livingEnvNote}
                  onChange={onFormChange}
                  maxLength={1000}
                  rows={3}
                />
              </label>
              <label>
                养宠经验
                <textarea
                  name="petExperienceNote"
                  value={form.petExperienceNote}
                  onChange={onFormChange}
                  maxLength={1000}
                  rows={3}
                />
              </label>
              <button className="primary-btn" type="submit" disabled={submitting}>
                {submitting ? "提交中..." : "确认提交"}
              </button>
            </form>
          ) : null}

          {submitMessage ? <p className="helper-text">{submitMessage}</p> : null}
        </aside>
      </section>
    </div>
  );
}

export default AdoptionDetailPage;
