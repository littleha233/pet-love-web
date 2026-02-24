import { useEffect, useState } from "react";
import { Link, useNavigate, useParams } from "react-router-dom";
import { getFeedingProviderDetail } from "../api/feedingApi";

function FeedingProviderDetailPage() {
  const { providerUserId } = useParams();
  const navigate = useNavigate();
  const [detail, setDetail] = useState(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState("");

  async function loadDetail() {
    setLoading(true);
    setError("");
    try {
      const data = await getFeedingProviderDetail(providerUserId);
      setDetail(data);
    } catch (err) {
      setError(err.message || "加载服务者详情失败");
    } finally {
      setLoading(false);
    }
  }

  useEffect(() => {
    loadDetail();
  }, [providerUserId]);

  if (loading) {
    return <p className="helper-text">加载中...</p>;
  }
  if (error) {
    return <p className="error-text">{error}</p>;
  }
  if (!detail) {
    return null;
  }

  function onCreateOrder() {
    navigate(`/feeding/orders/new?providerUserId=${detail.providerUserId}`);
  }

  return (
    <div className="page-stack">
      <section className="card page-banner">
        <p className="eyebrow">服务者详情</p>
        <h1>{detail.displayName}</h1>
        <p>{detail.headline || "暂无个性签名"}</p>
      </section>

      <section className="detail-layout">
        <article className="card detail-main">
          <div className="feeding-provider-top">
            {detail.avatarUrl ? (
              <img className="feeding-avatar" src={detail.avatarUrl} alt={detail.displayName} />
            ) : (
              <div className="feeding-avatar feeding-avatar-placeholder">头像</div>
            )}
            <div>
              <p className="helper-text">服务城市：{detail.serviceCityName}</p>
              <p className="helper-text">评分：{detail.ratingAvg || 0}（{detail.ratingCount || 0}）</p>
              <p className="helper-text">已完成订单：{detail.completedOrderCount || 0}</p>
            </div>
          </div>

          <h3>服务介绍</h3>
          <p>{detail.intro || "暂无介绍"}</p>

          <h3>可服务宠物</h3>
          <div className="tag-row">
            {(detail.servicePetTypes || []).map((item) => (
              <span key={item} className="soft-tag">
                {item}
              </span>
            ))}
          </div>

          <h3>服务内容</h3>
          <div className="tag-row">
            {(detail.serviceItemTags || []).map((item) => (
              <span key={item} className="soft-tag soft-tag-active">
                {item}
              </span>
            ))}
          </div>

          <p>参考价格：{detail.basePricePerVisit ? `¥${detail.basePricePerVisit}/次` : "面议"}</p>
          <p>从业年限：{detail.experienceYears ?? "-"}</p>
          <p>接单说明：{detail.acceptNotes || "-"}</p>
          <p>服务区域：{(detail.serviceDistricts || []).join(" / ") || "全城"}</p>
        </article>

        <aside className="card detail-side">
          <h3>预约操作</h3>
          {detail.viewerContext?.canCreateOrder ? (
            <button className="primary-btn full-btn" type="button" onClick={onCreateOrder}>
              发起预约请求
            </button>
          ) : (
            <>
              <button className="secondary-btn full-btn" type="button" disabled>
                暂不可预约
              </button>
              <p className="helper-text">{detail.viewerContext?.cannotCreateOrderReason || "请先登录"}</p>
            </>
          )}

          <Link className="secondary-btn full-btn" to="/feeding/providers">
            返回服务者列表
          </Link>
        </aside>
      </section>
    </div>
  );
}

export default FeedingProviderDetailPage;
