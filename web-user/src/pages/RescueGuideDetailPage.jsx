import { useEffect, useMemo, useState } from "react";
import { Link, useParams } from "react-router-dom";
import { getRescueGuideDetail } from "../api/rescueApi";
import { formatDateTime, formatRescueGuideScenario } from "../utils/format";
import { markdownToHtml } from "../utils/markdown";

const CITY_NAME_MAP = {
  "310100": "上海",
  "330100": "杭州",
  "320100": "南京",
  "440300": "深圳"
};

function RescueGuideDetailPage() {
  const { guideId } = useParams();
  const [detail, setDetail] = useState(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState("");

  useEffect(() => {
    let cancelled = false;

    async function loadData() {
      setLoading(true);
      setError("");
      try {
        const data = await getRescueGuideDetail(guideId);
        if (!cancelled) {
          setDetail(data);
        }
      } catch (err) {
        if (!cancelled) {
          setError(err.message || "加载救助指引详情失败");
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
  }, [guideId]);

  const html = useMemo(() => markdownToHtml(detail?.contentMd || ""), [detail?.contentMd]);

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
        <p className="eyebrow">救助指引详情</p>
        <h1>{detail.title}</h1>
        <p>
          场景：{formatRescueGuideScenario(detail.scenarioCode)} · 城市：
          {detail.cityName || CITY_NAME_MAP[detail.cityCode] || "全国通用"}
        </p>
      </section>

      <section className="detail-layout-single">
        <article className="card detail-main">
          <div className="tag-row">
            {(detail.tags || []).map((tag) => (
              <span key={tag} className="soft-tag">
                {tag}
              </span>
            ))}
          </div>
          <p className="helper-text">发布时间：{formatDateTime(detail.publishedAt)}</p>
          <p className="helper-text">更新时间：{formatDateTime(detail.updatedAt)}</p>
          <p>{detail.summary || ""}</p>

          <div className="markdown-content" dangerouslySetInnerHTML={{ __html: html }} />
        </article>

        <aside className="card detail-side">
          <h3>相关操作</h3>
          <div className="action-row">
            <Link
              className="primary-btn"
              to={detail.cityCode ? `/rescue/resources?cityCode=${detail.cityCode}` : "/rescue/resources"}
            >
              查看同城资源
            </Link>
            <Link className="secondary-btn" to="/rescue/clues/new">
              提交救助线索
            </Link>
            <Link className="secondary-btn" to="/rescue/guides">
              返回指引列表
            </Link>
          </div>
        </aside>
      </section>
    </div>
  );
}

export default RescueGuideDetailPage;
