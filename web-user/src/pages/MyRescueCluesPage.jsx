import { useEffect, useState } from "react";
import { Link } from "react-router-dom";
import Pagination from "../components/Pagination.jsx";
import RescueClueStatusTag from "../components/RescueClueStatusTag.jsx";
import { listMyRescueClues } from "../api/rescueApi";
import { formatDateTime, formatPetType, formatRescueUrgency } from "../utils/format";

const PAGE_SIZE = 10;

function MyRescueCluesPage() {
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
        const data = await listMyRescueClues({
          page,
          pageSize: PAGE_SIZE,
          status: status || undefined
        });
        if (!cancelled) {
          setResult(data);
        }
      } catch (err) {
        if (!cancelled) {
          setError(err.message || "加载我的线索失败");
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
    setPage(1);
    setStatus(event.target.value);
  }

  return (
    <div className="page-stack">
      <section className="card page-banner">
        <p className="eyebrow">我的线索</p>
        <h1>查看救助线索处理进度</h1>
        <p>仅可查看本人提交的线索，状态会随平台处理进展持续更新。</p>
      </section>

      <section className="card page-form-card">
        <div className="inline-filter-form">
          <select value={status} onChange={onStatusChange}>
            <option value="">全部状态</option>
            <option value="SUBMITTED">已提交</option>
            <option value="TRIAGED">已分流</option>
            <option value="IN_PROGRESS">跟进中</option>
            <option value="RESOLVED">已解决</option>
            <option value="CLOSED">已关闭</option>
            <option value="INVALID">无效线索</option>
          </select>
          <Link className="secondary-btn" to="/rescue/clues/new">
            新建线索
          </Link>
        </div>

        {loading ? <p className="helper-text">加载中...</p> : null}
        {error ? <p className="error-text">{error}</p> : null}

        <div className="list-stack">
          {result.items.map((item) => (
            <article key={item.clueId} className="list-card">
              <div className="list-card-main">
                <h3>线索编号：{item.clueNo}</h3>
                <p className="helper-text">
                  {item.cityName}
                  {item.districtName ? ` · ${item.districtName}` : ""} · {formatPetType(item.petType)}
                </p>
                <p className="helper-text">
                  紧急程度：{formatRescueUrgency(item.urgencyLevel)} · 更新时间：{formatDateTime(item.updatedAt)}
                </p>
                <div className="tag-row">
                  <RescueClueStatusTag status={item.status} />
                  <span className="soft-tag">提交时间：{formatDateTime(item.createdAt)}</span>
                </div>
              </div>
              <div className="list-card-actions">
                <Link className="secondary-btn" to={`/me/rescue/clues/${item.clueId}`}>
                  查看详情
                </Link>
              </div>
            </article>
          ))}
        </div>

        {!loading && !error && result.items.length === 0 ? (
          <p className="helper-text">暂无线索记录。</p>
        ) : null}

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

export default MyRescueCluesPage;
