import { useEffect, useState } from "react";
import { Link } from "react-router-dom";
import Pagination from "../components/Pagination.jsx";
import PostStatusTag from "../components/PostStatusTag.jsx";
import { listAdminAdoptionPosts } from "../api/adoptionApi";
import { formatDateTime } from "../utils/format";

const PAGE_SIZE = 20;

function AdminAdoptionPostReviewListPage() {
  const [draft, setDraft] = useState({
    status: "",
    cityCode: "",
    petType: "",
    keyword: "",
    dateFrom: "",
    dateTo: ""
  });
  const [filters, setFilters] = useState(draft);
  const [page, setPage] = useState(1);
  const [result, setResult] = useState({ items: [], total: 0, page: 1, pageSize: PAGE_SIZE });
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState("");

  useEffect(() => {
    let cancelled = false;

    async function loadData() {
      setLoading(true);
      setError("");
      try {
        const data = await listAdminAdoptionPosts({
          page,
          pageSize: PAGE_SIZE,
          ...filters
        });
        if (!cancelled) {
          setResult(data);
        }
      } catch (err) {
        if (!cancelled) {
          setError(err.message || "加载失败");
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
  }, [filters, page]);

  function onChange(event) {
    const { name, value } = event.target;
    setDraft((prev) => ({ ...prev, [name]: value }));
  }

  function onSubmit(event) {
    event.preventDefault();
    setPage(1);
    setFilters({ ...draft });
  }

  return (
    <div className="page-stack">
      <section className="card page-banner">
        <p className="eyebrow">审核列表</p>
        <h1>送养帖审核管理</h1>
        <p>在这里处理待审核的送养帖子。</p>
      </section>

      <section className="card page-form-card">
        <form className="inline-filter-form" onSubmit={onSubmit}>
          <select name="status" value={draft.status} onChange={onChange}>
            <option value="">全部状态</option>
            <option value="PENDING_REVIEW">待审核</option>
            <option value="PUBLISHED">已发布</option>
            <option value="REJECTED">已驳回</option>
            <option value="CLOSED">已关闭</option>
            <option value="OFFLINE">已下架</option>
          </select>
          <input name="cityCode" value={draft.cityCode} onChange={onChange} placeholder="城市编码" />
          <select name="petType" value={draft.petType} onChange={onChange}>
            <option value="">全部宠物</option>
            <option value="CAT">CAT</option>
            <option value="DOG">DOG</option>
          </select>
          <input name="keyword" value={draft.keyword} onChange={onChange} placeholder="关键词" />
          <input name="dateFrom" type="date" value={draft.dateFrom} onChange={onChange} />
          <input name="dateTo" type="date" value={draft.dateTo} onChange={onChange} />
          <button type="submit" className="primary-btn">
            查询
          </button>
        </form>

        {loading ? <p className="helper-text">加载中...</p> : null}
        {error ? <p className="error-text">{error}</p> : null}

        <div className="list-stack">
          {result.items.map((item) => (
            <article key={item.postId} className="list-card">
              <div className="list-card-main">
                <h3>
                  #{item.postId} {item.title}
                </h3>
                <p className="helper-text">
                  发布者：{item.publisherNickname}（{item.publisherUserId}） · 城市：{item.cityName} · 宠物：
                  {item.petType || "-"}
                </p>
                <p className="helper-text">
                  版本：{item.submitVersion} · 更新时间：{formatDateTime(item.updatedAt)}
                </p>
                <div className="tag-row">
                  <PostStatusTag status={item.status} />
                  {item.reviewedByAdminName ? (
                    <span className="soft-tag">
                      审核人：{item.reviewedByAdminName}（{formatDateTime(item.reviewedAt)}）
                    </span>
                  ) : null}
                </div>
              </div>
              <div className="list-card-actions">
                <Link className="secondary-btn" to={`/admin/adoptions/posts/${item.postId}`}>
                  查看详情
                </Link>
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

export default AdminAdoptionPostReviewListPage;
