import { useEffect, useState } from "react";
import { Link } from "react-router-dom";
import Pagination from "../components/Pagination.jsx";
import RescueGuideStatusTag from "../components/RescueGuideStatusTag.jsx";
import { listAdminRescueGuides } from "../api/rescueApi";
import { formatDateTime } from "../utils/format";

const PAGE_SIZE = 20;

function AdminRescueGuideListPage() {
  const [draft, setDraft] = useState({ status: "", scenarioCode: "", cityCode: "", keyword: "" });
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
        const data = await listAdminRescueGuides({ page, pageSize: PAGE_SIZE, ...filters });
        if (!cancelled) {
          setResult(data);
        }
      } catch (err) {
        if (!cancelled) {
          setError(err.message || "加载救助指引管理列表失败");
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
  }, [page, filters]);

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
        <p className="eyebrow">Admin 救助指引</p>
        <h1>救助指引管理</h1>
        <p>可筛选、查看、编辑并发布/下线指引内容。</p>
      </section>

      <section className="card page-form-card">
        <form className="inline-filter-form" onSubmit={onSubmit}>
          <select name="status" value={draft.status} onChange={onChange}>
            <option value="">全部状态</option>
            <option value="DRAFT">草稿</option>
            <option value="PUBLISHED">已发布</option>
            <option value="OFFLINE">已下线</option>
          </select>
          <input name="scenarioCode" value={draft.scenarioCode} onChange={onChange} placeholder="场景编码" />
          <input name="cityCode" value={draft.cityCode} onChange={onChange} placeholder="城市编码" />
          <input name="keyword" value={draft.keyword} onChange={onChange} placeholder="关键词" />
          <button className="primary-btn" type="submit">
            查询
          </button>
          <Link className="secondary-btn" to="/admin/rescue/guides/new">
            新建指引
          </Link>
        </form>

        {loading ? <p className="helper-text">加载中...</p> : null}
        {error ? <p className="error-text">{error}</p> : null}

        <div className="list-stack">
          {result.items.map((item) => (
            <article key={item.guideId} className="list-card">
              <div className="list-card-main">
                <h3>
                  #{item.guideId} {item.title}
                </h3>
                <p className="helper-text">
                  场景：{item.scenarioCode} · 城市：{item.cityCode || "全国通用"} · 版本：{item.version}
                </p>
                <p className="helper-text">更新时间：{formatDateTime(item.updatedAt)}</p>
                <div className="tag-row">
                  <RescueGuideStatusTag status={item.status} />
                  <span className="soft-tag">发布时间：{formatDateTime(item.publishedAt)}</span>
                </div>
              </div>
              <div className="list-card-actions">
                <Link className="secondary-btn" to={`/admin/rescue/guides/${item.guideId}`}>
                  编辑
                </Link>
              </div>
            </article>
          ))}
        </div>

        {!loading && !error && result.items.length === 0 ? <p className="helper-text">暂无数据</p> : null}

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

export default AdminRescueGuideListPage;
