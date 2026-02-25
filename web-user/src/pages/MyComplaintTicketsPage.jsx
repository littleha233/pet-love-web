import { useEffect, useState } from "react";
import { Link } from "react-router-dom";
import Pagination from "../components/Pagination.jsx";
import ComplaintStatusTag from "../components/ComplaintStatusTag.jsx";
import ComplaintPriorityTag from "../components/ComplaintPriorityTag.jsx";
import { listMyComplaintTickets } from "../api/opsApi";
import { formatDateTime } from "../utils/format";

const PAGE_SIZE = 20;

function MyComplaintTicketsPage() {
  const [status, setStatus] = useState("");
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
        const data = await listMyComplaintTickets({ page, pageSize: PAGE_SIZE, status: status || undefined });
        if (!cancelled) {
          setResult(data);
        }
      } catch (err) {
        if (!cancelled) {
          setError(err.message || "加载我的投诉失败");
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
  }, [page, status]);

  function onStatusChange(event) {
    setStatus(event.target.value);
    setPage(1);
  }

  return (
    <div className="page-stack">
      <section className="card page-banner">
        <p className="eyebrow">帮助与投诉</p>
        <h1>我的投诉工单</h1>
        <p>
          可按状态筛选工单，并查看平台回复进度。<Link to="/support/complaints/new">去提交新投诉</Link>
        </p>
      </section>

      <section className="card page-form-card">
        <div className="inline-filter-form">
          <select value={status} onChange={onStatusChange}>
            <option value="">全部状态</option>
            <option value="SUBMITTED">已提交</option>
            <option value="IN_REVIEW">处理中</option>
            <option value="WAITING_USER">待补充</option>
            <option value="RESOLVED">已解决</option>
            <option value="REJECTED">已驳回</option>
            <option value="CLOSED">已关闭</option>
            <option value="CANCELLED_BY_USER">已取消</option>
          </select>
          <Link className="primary-btn" to="/support/complaints/new">
            提交投诉
          </Link>
        </div>

        {loading ? <p className="helper-text">加载中...</p> : null}
        {error ? <p className="error-text">{error}</p> : null}

        <div className="list-stack">
          {result.items.map((item) => (
            <article key={item.ticketId} className="list-card">
              <div className="list-card-main">
                <h3>#{item.ticketNo}</h3>
                <p>{item.title}</p>
                <p className="helper-text">对象类型：{item.targetType || "-"}</p>
                <p className="helper-text">
                  最后回复：{formatDateTime(item.lastReplyAt)} · 更新时间：{formatDateTime(item.updatedAt)}
                </p>
                <div className="tag-row">
                  <ComplaintStatusTag status={item.status} />
                  <ComplaintPriorityTag priority={item.priority} />
                </div>
              </div>
              <div className="list-card-actions">
                <Link className="secondary-btn" to={`/me/support/complaints/${item.ticketId}`}>
                  查看详情
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

export default MyComplaintTicketsPage;
