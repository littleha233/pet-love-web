import { useEffect, useState } from "react";
import { Link, useLocation } from "react-router-dom";
import Pagination from "../components/Pagination.jsx";
import PostStatusTag from "../components/PostStatusTag.jsx";
import {
  closeRehomePost,
  listMyRehomePosts,
  resubmitRehomePost
} from "../api/adoptionApi";
import { formatDateTime, formatPublicText } from "../utils/format";

const PAGE_SIZE = 10;

function MyRehomePostsPage() {
  const location = useLocation();
  const [status, setStatus] = useState("");
  const [page, setPage] = useState(1);
  const [result, setResult] = useState({ items: [], total: 0, page: 1, pageSize: PAGE_SIZE });
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState("");
  const [notice, setNotice] = useState(location.state?.notice || "");

  async function loadData() {
    setLoading(true);
    setError("");
    try {
      const data = await listMyRehomePosts({ page, pageSize: PAGE_SIZE, status: status || undefined });
      setResult(data);
    } catch (err) {
      setError(err.message || "加载失败");
    } finally {
      setLoading(false);
    }
  }

  useEffect(() => {
    loadData();
  }, [page, status]);

  async function onResubmit(postId) {
    try {
      await resubmitRehomePost(postId);
      setNotice("帖子已重新提交。");
      await loadData();
    } catch (err) {
      setNotice(err.message || "重提失败");
    }
  }

  async function onClose(postId) {
    if (!window.confirm("确认关闭该帖子？")) {
      return;
    }
    try {
      await closeRehomePost(postId);
      setNotice("帖子已关闭。");
      await loadData();
    } catch (err) {
      setNotice(err.message || "关闭失败");
    }
  }

  return (
    <div className="page-stack">
      <section className="card page-banner">
        <p className="eyebrow">我的发布</p>
        <h1>查看帖子状态与处理进度</h1>
      </section>

      <section className="card page-form-card">
        <div className="inline-filter-form">
          <select
            value={status}
            onChange={(event) => {
              setStatus(event.target.value);
              setPage(1);
            }}
          >
            <option value="">全部状态</option>
            <option value="PENDING_REVIEW">待审核</option>
            <option value="PUBLISHED">已发布</option>
            <option value="REJECTED">已驳回</option>
            <option value="CLOSED">已关闭</option>
            <option value="OFFLINE">已下架</option>
          </select>
          <Link to="/adoption/post/new" className="primary-btn">
            新建送养帖
          </Link>
        </div>

        {loading ? <p className="helper-text">加载中...</p> : null}
        {error ? <p className="error-text">{error}</p> : null}
        {notice ? <p className="helper-text notice-text">{notice}</p> : null}

        <div className="list-stack">
          {result.items.map((item) => (
            <article key={item.postId} className="list-card">
              <div className="list-card-main">
                <h3>
                  <Link to={`/adoption/${item.postId}`}>{formatPublicText(item.title)}</Link>
                </h3>
                <p className="helper-text">
                  {item.cityName} · 申请数 {item.applicationCount} · 更新时间 {formatDateTime(item.updatedAt)}
                </p>
                <div className="tag-row">
                  <PostStatusTag status={item.status} />
                  {item.publishedAt ? <span className="soft-tag">发布于 {formatDateTime(item.publishedAt)}</span> : null}
                </div>
                {item.rejectReasonText ? <p className="error-text">驳回原因：{item.rejectReasonText}</p> : null}
              </div>
              <div className="list-card-actions">
                <Link className="secondary-btn" to={`/adoption/my-posts/${item.postId}/applications`}>
                  查看收到的申请
                </Link>
                {item.status === "REJECTED" ? (
                  <button className="secondary-btn" type="button" onClick={() => onResubmit(item.postId)}>
                    重新提交
                  </button>
                ) : null}
                {item.status === "PUBLISHED" ? (
                  <button className="secondary-btn" type="button" onClick={() => onClose(item.postId)}>
                    关闭帖子
                  </button>
                ) : null}
              </div>
            </article>
          ))}
        </div>

        <Pagination
          page={result.page || page}
          pageSize={result.pageSize || PAGE_SIZE}
          total={result.total || 0}
          onChange={setPage}
        />
      </section>
    </div>
  );
}

export default MyRehomePostsPage;
