import { useEffect, useState } from "react";
import { Link, useParams } from "react-router-dom";
import { getRescueResourceDetail } from "../api/rescueApi";
import { formatDateTime, formatPetType, formatRescueResourceType } from "../utils/format";

function RescueResourceDetailPage() {
  const { resourceId } = useParams();
  const [detail, setDetail] = useState(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState("");

  useEffect(() => {
    let cancelled = false;

    async function loadData() {
      setLoading(true);
      setError("");
      try {
        const data = await getRescueResourceDetail(resourceId);
        if (!cancelled) {
          setDetail(data);
        }
      } catch (err) {
        if (!cancelled) {
          setError(err.message || "加载资源详情失败");
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
  }, [resourceId]);

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
        <p className="eyebrow">资源详情</p>
        <h1>{detail.name}</h1>
        <p>
          类型：{formatRescueResourceType(detail.resourceType)} · {detail.cityName || "未标注城市"}
          {detail.districtName ? ` · ${detail.districtName}` : ""}
        </p>
      </section>

      <section className="detail-layout">
        <article className="card detail-main">
          <h3>联系方式</h3>
          <p>电话：{detail.contactPhone || "-"}</p>
          <p>微信：{detail.contactWechat || "-"}</p>
          <p>其他：{detail.contactOther || "-"}</p>

          <h3>地址与服务</h3>
          <p>地址：{detail.address || "-"}</p>
          <p>服务时间：{detail.serviceHours || "-"}</p>
          <p>服务范围：{detail.serviceScope || "-"}</p>

          <div className="tag-row">
            {(detail.acceptPetTypes || []).map((item) => (
              <span key={item} className="soft-tag">
                {formatPetType(item)}
              </span>
            ))}
            {(detail.capabilityTags || []).map((item) => (
              <span key={item} className="soft-tag soft-tag-active">
                {item}
              </span>
            ))}
          </div>

          <h3>说明</h3>
          <p>{detail.description || "暂无说明"}</p>
          {detail.sourceUrl ? (
            <p>
              机构主页：
              <a href={detail.sourceUrl} target="_blank" rel="noreferrer">
                查看链接
              </a>
            </p>
          ) : null}
          <p>核验时间：{formatDateTime(detail.verifiedAt)}</p>
        </article>

        <aside className="card detail-side">
          <h3>操作</h3>
          <div className="action-row">
            <Link className="primary-btn" to="/rescue/clues/new">
              提交救助线索
            </Link>
            <Link className="secondary-btn" to="/rescue/resources">
              返回资源目录
            </Link>
          </div>
        </aside>
      </section>
    </div>
  );
}

export default RescueResourceDetailPage;
