import { useEffect, useState } from "react";
import { Link } from "react-router-dom";
import Pagination from "../components/Pagination.jsx";
import RescueClueStatusTag from "../components/RescueClueStatusTag.jsx";
import { listAdminRescueClues } from "../api/rescueApi";
import { formatDateTime } from "../utils/format";

const PAGE_SIZE = 20;

function AdminRescueClueListPage() {
  const [draft, setDraft] = useState({
    status: "",
    urgencyLevel: "",
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
        const data = await listAdminRescueClues({ page, pageSize: PAGE_SIZE, ...filters });
        if (!cancelled) {
          setResult(data);
        }
      } catch (err) {
        if (!cancelled) {
          setError(err.message || "加载线索管理列表失败");
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
        <p className="eyebrow">Admin 线索分流</p>
        <h1>救助线索管理</h1>
        <p>支持按状态、紧急程度、城市和时间筛选，进入详情后更新状态。</p>
      </section>

      <section className="card page-form-card">
        <form className="inline-filter-form" onSubmit={onSubmit}>
          <select name="status" value={draft.status} onChange={onChange}>
            <option value="">全部状态</option>
            <option value="SUBMITTED">SUBMITTED</option>
            <option value="TRIAGED">TRIAGED</option>
            <option value="IN_PROGRESS">IN_PROGRESS</option>
            <option value="RESOLVED">RESOLVED</option>
            <option value="CLOSED">CLOSED</option>
            <option value="INVALID">INVALID</option>
          </select>
          <select name="urgencyLevel" value={draft.urgencyLevel} onChange={onChange}>
            <option value="">全部紧急程度</option>
            <option value="LOW">LOW</option>
            <option value="MEDIUM">MEDIUM</option>
            <option value="HIGH">HIGH</option>
            <option value="EMERGENCY">EMERGENCY</option>
          </select>
          <input name="cityCode" value={draft.cityCode} onChange={onChange} placeholder="城市编码" />
          <select name="petType" value={draft.petType} onChange={onChange}>
            <option value="">全部宠物类型</option>
            <option value="CAT">CAT</option>
            <option value="DOG">DOG</option>
            <option value="UNKNOWN">UNKNOWN</option>
          </select>
          <input name="keyword" value={draft.keyword} onChange={onChange} placeholder="编号/联系人/位置" />
          <input name="dateFrom" type="date" value={draft.dateFrom} onChange={onChange} />
          <input name="dateTo" type="date" value={draft.dateTo} onChange={onChange} />
          <button className="primary-btn" type="submit">
            查询
          </button>
        </form>

        {loading ? <p className="helper-text">加载中...</p> : null}
        {error ? <p className="error-text">{error}</p> : null}

        <div className="list-stack">
          {result.items.map((item) => (
            <article key={item.clueId} className="list-card">
              <div className="list-card-main">
                <h3>
                  #{item.clueNo}
                </h3>
                <p className="helper-text">
                  城市：{item.cityName}
                  {item.districtName ? ` · ${item.districtName}` : ""} · 紧急程度：{item.urgencyLevel}
                </p>
                <p className="helper-text">
                  提交用户：{item.reporterUserId} · 处理人：{item.handledByAdminName || "-"}
                </p>
                <p className="helper-text">
                  处理时间：{formatDateTime(item.handledAt)} · 提交时间：{formatDateTime(item.createdAt)}
                </p>
                <div className="tag-row">
                  <RescueClueStatusTag status={item.status} />
                  <span className="soft-tag">宠物：{item.petType || "UNKNOWN"}</span>
                </div>
              </div>
              <div className="list-card-actions">
                <Link className="secondary-btn" to={`/admin/rescue/clues/${item.clueId}`}>
                  处理详情
                </Link>
              </div>
            </article>
          ))}
        </div>

        {!loading && !error && result.items.length === 0 ? <p className="helper-text">暂无线索</p> : null}

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

export default AdminRescueClueListPage;
