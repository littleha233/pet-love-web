import { Link } from "react-router-dom";
import PostStatusTag from "./PostStatusTag";
import { formatDateTime, formatNumber } from "../utils/format";

function AdoptionPostCard({ post }) {
  return (
    <article className="adoption-post-card card">
      <Link to={`/adoption/${post.postId}`} className="adoption-post-cover">
        {post.coverImageUrl ? (
          <img src={post.coverImageUrl} alt={post.title} />
        ) : (
          <div className="image-placeholder">暂无图片</div>
        )}
      </Link>
      <div className="adoption-post-content">
        <div className="status-line">
          <PostStatusTag status={post.status || "PUBLISHED"} />
          <span>{formatDateTime(post.publishedAt)}</span>
        </div>
        <Link to={`/adoption/${post.postId}`} className="adoption-post-title">
          {post.title}
        </Link>
        <p className="adoption-post-meta">
          {post.cityName || "未知城市"}
          {post.districtName ? ` · ${post.districtName}` : ""}
          {post.petType ? ` · ${post.petType}` : ""}
          {post.petName ? ` · ${post.petName}` : ""}
        </p>
        <div className="tag-row">
          {(post.temperamentTags || []).slice(0, 4).map((tag) => (
            <span key={tag} className="soft-tag">
              {tag}
            </span>
          ))}
        </div>
        <p className="helper-text">浏览量：{formatNumber(post.viewCount || 0)}</p>
      </div>
    </article>
  );
}

export default AdoptionPostCard;
