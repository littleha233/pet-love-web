import { useEffect, useState } from "react";
import { Link } from "react-router-dom";
import Pagination from "../components/Pagination.jsx";
import RescueResourceStatusTag from "../components/RescueResourceStatusTag.jsx";
import { listAdminRescueResources } from "../api/rescueApi";
import { formatDateTime } from "../utils/format";

const PAGE_SIZE = 20;

function AdminRescueResourceListPage() {
  const [draft, setDraft] = useState({ status: "", cityCode: "", resourceType: "", keyword: "" });
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
        const data = await listAdminRescueResources({ page, pageSize: PAGE_SIZE, ...filters });
        if (!cancelled) {
          setResult(data);
        }
      } catch (err) {
        if (!cancelled) {
          setError(err.message || "加载资源管理列表失败");
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
        <p className="eyebrow">Admin 资源目录</p>
        <h1>救助资源管理</h1>
        <p>维护医院、救助站、志愿者组织和官方渠道信息。</p>
      </section>

      <section className="card page-form-card">
        <form className="inline-filter-form" onSubmit={onSubmit}>
          <select name="status" value={draft.status} onChange={onChange}>
            <option value="">全部状态</option>
            <option value="DRAFT">草稿</option>
            <option value="ACTIVE">启用</option>
            <option value="PAUSED">暂停</option>
            <option value="OFFLINE">下线</option>
          </select>
          <input name="cityCode" value={draft.cityCode} onChange={onChange} placeholder="城市编码" />
          <select name="resourceType" value={draft.resourceType} onChange={onChange}>
            <option value="">全部类型</option>
            <option value="ANIMAL_HOSPITAL">ANIMAL_HOSPITAL</option>
            <option value="SHELTER">SHELTER</option>
            <option value="VOLUNTEER_GROUP">VOLUNTEER_GROUP</option>
            <option value="OFFICIAL_CHANNEL">OFFICIAL_CHANNEL</option>
            <option value="NGO">NGO</option>
          </select>
          <input name="keyword" value={draft.keyword} onChange={onChange} placeholder="关键词" />
          <button className="primary-btn" type="submit">
            查询
          </button>
          <Link className="secondary-btn" to="/admin/rescue/resources/new">
            新建资源
          </Link>
        </form>

        {loading ? <p className="helper-text">加载中...</p> : null}
        {error ? <p className="error-text">{error}</p> : null}

        <div className="list-stack">
          {result.items.map((item) => (
            <article key={item.resourceId} className="list-card">
              <div className="list-card-main">
                <h3>
                  #{item.resourceId} {item.name}
                </h3>
                <p className="helper-text">
                  类型：{item.resourceType} · 城市：{item.cityName}
                </p>
                <p className="helper-text">更新时间：{formatDateTime(item.updatedAt)}</p>
                <div className="tag-row">
                  <RescueResourceStatusTag status={item.status} />
                  <span className="soft-tag">核验：{formatDateTime(item.verifiedAt)}</span>
                </div>
              </div>
              <div className="list-card-actions">
                <Link className="secondary-btn" to={`/admin/rescue/resources/${item.resourceId}`}>
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

export default AdminRescueResourceListPage;
