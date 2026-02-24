import { Link } from "react-router-dom";

function FeedingProviderCard({ provider }) {
  return (
    <article className="card feeding-provider-card">
      <div className="feeding-provider-top">
        {provider.avatarUrl ? (
          <img className="feeding-avatar" src={provider.avatarUrl} alt={provider.displayName} />
        ) : (
          <div className="feeding-avatar feeding-avatar-placeholder">头像</div>
        )}
        <div>
          <h3>{provider.displayName}</h3>
          <p className="helper-text">{provider.serviceCityName}</p>
          <p className="helper-text">评分 {provider.ratingAvg || 0}（{provider.ratingCount || 0}）</p>
        </div>
      </div>
      {provider.headline ? <p>{provider.headline}</p> : null}
      <div className="tag-row">
        {(provider.servicePetTypes || []).map((tag) => (
          <span key={tag} className="soft-tag">
            {tag}
          </span>
        ))}
      </div>
      <div className="tag-row">
        {(provider.serviceItemTags || []).map((tag) => (
          <span key={tag} className="soft-tag soft-tag-active">
            {tag}
          </span>
        ))}
      </div>
      <div className="status-line">
        <span>参考价：{provider.basePricePerVisit ? `¥${provider.basePricePerVisit}/次` : "面议"}</span>
        <span>完成订单：{provider.completedOrderCount || 0}</span>
      </div>
      <Link className="primary-btn full-btn" to={`/feeding/providers/${provider.providerUserId}`}>
        查看详情
      </Link>
    </article>
  );
}

export default FeedingProviderCard;
