import { useEffect, useState } from "react";
import { Link } from "react-router-dom";
import Pagination from "../components/Pagination.jsx";
import ComplaintStatusTag from "../components/ComplaintStatusTag.jsx";
import ComplaintPriorityTag from "../components/ComplaintPriorityTag.jsx";
import { listAdminComplaintTickets } from "../api/opsApi";
import { formatDateTime } from "../utils/format";

const PAGE_SIZE = 20;

function AdminComplaintTicketListPage() {
  const [draft, setDraft] = useState({
    status: "",
    priority: "",
    targetType: "",
    assignedAdminId: "",
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
        const data = await listAdminComplaintTickets({ page, pageSize: PAGE_SIZE, ...filters });
        if (!cancelled) {
          setResult(data);
        }
      } catch (err) {
        if (!cancelled) {
          setError(err.message || "加载投诉工单列表失败");
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
        <p className="eyebrow">Admin Ops</p>
        <h1>投诉工单管理</h1>
        <p>支持按状态、优先级、对象类型、时间筛选，进入详情处理回复与状态流转。</p>
      </section>

      <section className="card page-form-card">
        <form className="inline-filter-form" onSubmit={onSubmit}>
          <select name="status" value={draft.status} onChange={onChange}>
            <option value="">全部状态</option>
            <option value="SUBMITTED">SUBMITTED</option>
            <option value="IN_REVIEW">IN_REVIEW</option>
            <option value="WAITING_USER">WAITING_USER</option>
            <option value="RESOLVED">RESOLVED</option>
            <option value="REJECTED">REJECTED</option>
            <option value="CLOSED">CLOSED</option>
            <option value="CANCELLED_BY_USER">CANCELLED_BY_USER</option>
          </select>
          <select name="priority" value={draft.priority} onChange={onChange}>
            <option value="">全部优先级</option>
            <option value="LOW">LOW</option>
            <option value="MEDIUM">MEDIUM</option>
            <option value="HIGH">HIGH</option>
            <option value="URGENT">URGENT</option>
          </select>
          <input name="targetType" value={draft.targetType} onChange={onChange} placeholder="对象类型" />
          <input name="assignedAdminId" value={draft.assignedAdminId} onChange={onChange} placeholder="处理人ID" />
          <input name="keyword" value={draft.keyword} onChange={onChange} placeholder="编号/标题/用户ID" />
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
            <article key={item.ticketId} className="list-card">
              <div className="list-card-main">
                <h3>#{item.ticketNo}</h3>
                <p>{item.title}</p>
                <p className="helper-text">
                  用户：{item.reporterUserId} · 对象：{item.targetType} · 处理人：{item.assignedAdminName || "-"}
                </p>
                <p className="helper-text">
                  最后回复：{formatDateTime(item.lastReplyAt)} · 提交时间：{formatDateTime(item.createdAt)}
                </p>
                <div className="tag-row">
                  <ComplaintStatusTag status={item.status} />
                  <ComplaintPriorityTag priority={item.priority} />
                </div>
              </div>
              <div className="list-card-actions">
                <Link className="secondary-btn" to={`/admin/ops/complaints/${item.ticketId}`}>
                  处理详情
                </Link>
              </div>
            </article>
          ))}
        </div>

        {!loading && !error && result.items.length === 0 ? <p className="helper-text">暂无工单</p> : null}

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

export default AdminComplaintTicketListPage;
