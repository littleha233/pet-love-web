import { useEffect, useState } from "react";
import { Link, useParams } from "react-router-dom";
import RescueClueStatusTag from "../components/RescueClueStatusTag.jsx";
import { getMyRescueClueDetail } from "../api/rescueApi";
import { formatDateTime } from "../utils/format";

function MyRescueClueDetailPage() {
  const { clueId } = useParams();
  const [detail, setDetail] = useState(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState("");

  useEffect(() => {
    let cancelled = false;

    async function loadData() {
      setLoading(true);
      setError("");
      try {
        const data = await getMyRescueClueDetail(clueId);
        if (!cancelled) {
          setDetail(data);
        }
      } catch (err) {
        if (!cancelled) {
          setError(err.message || "加载线索详情失败");
        }
      } finally {
        if (!cancelled) {
          setLoading(false);
        }
      }
    }

    loadData();
    return () => {
      cancelled = true;
    };
  }, [clueId]);

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
        <p className="eyebrow">线索详情</p>
        <h1>{detail.clueNo}</h1>
        <p>
          {detail.cityName}
          {detail.districtName ? ` · ${detail.districtName}` : ""}
        </p>
      </section>

      <section className="detail-layout">
        <article className="card detail-main">
          <div className="tag-row">
            <RescueClueStatusTag status={detail.status} />
            <span className="soft-tag">紧急程度：{detail.urgencyLevel}</span>
            <span className="soft-tag">宠物类型：{detail.petType || "UNKNOWN"}</span>
          </div>

          <p>地点：{detail.locationText}</p>
          <p>数量：{detail.estimatedCount ?? "-"}</p>
          <p>情况标签：{(detail.conditionTags || []).join(" / ") || "-"}</p>
          <p>描述：{detail.description}</p>
          <p>
            联系人：{detail.contactName} · 电话：{detail.contactMobileMasked}
          </p>
          <p>分流备注：{detail.triageNote || "-"}</p>
          <p>处理结果备注：{detail.resolutionNote || "-"}</p>
          <p>提交时间：{formatDateTime(detail.createdAt)}</p>
          <p>更新时间：{formatDateTime(detail.updatedAt)}</p>

          <h3>现场图片</h3>
          <div className="detail-image-grid">
            {(detail.photos || []).map((photo) => (
              <img key={`${photo.fileId}-${photo.sortOrder}`} src={photo.url} alt="线索图片" />
            ))}
          </div>
          {(detail.photos || []).length === 0 ? <p className="helper-text">未上传图片</p> : null}
        </article>

        <aside className="card detail-side">
          <h3>推荐资源</h3>
          {(detail.suggestedResources || []).length === 0 ? <p className="helper-text">暂无推荐资源</p> : null}
          <div className="list-stack">
            {(detail.suggestedResources || []).map((resource) => (
              <article key={resource.resourceId} className="list-card">
                <div className="list-card-main">
                  <h3>{resource.name}</h3>
                  <p className="helper-text">
                    类型：{resource.resourceType || "-"} · 电话：{resource.contactPhone || "-"}
                  </p>
                </div>
                <div className="list-card-actions">
                  <Link className="secondary-btn" to={`/rescue/resources/${resource.resourceId}`}>
                    查看
                  </Link>
                </div>
              </article>
            ))}
          </div>

          <div className="action-row">
            <Link className="secondary-btn" to="/me/rescue/clues">
              返回我的线索
            </Link>
            <Link className="secondary-btn" to="/rescue/resources">
              浏览资源目录
            </Link>
          </div>
        </aside>
      </section>
    </div>
  );
}

export default MyRescueClueDetailPage;
