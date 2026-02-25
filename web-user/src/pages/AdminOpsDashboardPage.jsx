import { useEffect, useState } from "react";
import { Link } from "react-router-dom";
import {
  getAdminOpsDashboardSummary,
  getAdminOpsDashboardTodos,
  getAdminOpsDashboardTrends
} from "../api/opsApi";
import { formatDateTime, formatNumber } from "../utils/format";

function AdminOpsDashboardPage() {
  const [summary, setSummary] = useState(null);
  const [todos, setTodos] = useState([]);
  const [trends, setTrends] = useState([]);
  const [metric, setMetric] = useState("COMPLAINTS");
  const [days, setDays] = useState("7");
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState("");

  useEffect(() => {
    let cancelled = false;

    async function loadData() {
      setLoading(true);
      setError("");
      try {
        const [summaryData, todoData, trendData] = await Promise.all([
          getAdminOpsDashboardSummary(),
          getAdminOpsDashboardTodos(),
          getAdminOpsDashboardTrends({ metric, days: Number(days) })
        ]);
        if (!cancelled) {
          setSummary(summaryData);
          setTodos(todoData || []);
          setTrends(trendData || []);
        }
      } catch (err) {
        if (!cancelled) {
          setError(err.message || "加载运营看板失败");
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
  }, [metric, days]);

  return (
    <div className="page-stack">
      <section className="card page-banner">
        <p className="eyebrow">Admin Ops</p>
        <h1>运营 Dashboard</h1>
        <p>展示今日新增、待办与趋势数据，便于快速定位运营优先级。</p>
      </section>

      <section className="card page-form-card">
        {loading ? <p className="helper-text">加载中...</p> : null}
        {error ? <p className="error-text">{error}</p> : null}

        {summary ? (
          <>
            <p className="helper-text">统计时间：{formatDateTime(summary.generatedAt)}</p>
            <div className="card-grid card-grid-three">
              <article className="service-card">
                <h3>今日新增用户</h3>
                <p>{formatNumber(summary.todayNewUsers)}</p>
              </article>
              <article className="service-card">
                <h3>今日新增投诉</h3>
                <p>{formatNumber(summary.todayNewComplaints)}</p>
              </article>
              <article className="service-card">
                <h3>待处理投诉</h3>
                <p>{formatNumber(summary.pendingComplaintCount)}</p>
              </article>
              <article className="service-card">
                <h3>待审核认证</h3>
                <p>{formatNumber(summary.pendingVerificationCount)}</p>
              </article>
              <article className="service-card">
                <h3>待审核送养帖</h3>
                <p>{formatNumber(summary.pendingAdoptionReviewCount)}</p>
              </article>
              <article className="service-card">
                <h3>待处理救助线索</h3>
                <p>{formatNumber(summary.pendingRescueClueCount)}</p>
              </article>
            </div>
          </>
        ) : null}
      </section>

      <section className="card page-form-card">
        <h3>待办事项</h3>
        <div className="list-stack">
          {(todos || []).map((todo) => (
            <article key={todo.todoType} className="list-card">
              <div className="list-card-main">
                <h3>{todo.title}</h3>
                <p className="helper-text">
                  类型：{todo.todoType} · 优先级：{todo.priority}
                </p>
                <p className="helper-text">数量：{formatNumber(todo.count)}</p>
              </div>
              <div className="list-card-actions">
                <Link className="secondary-btn" to={todo.targetRoute || "/"}>
                  前往处理
                </Link>
              </div>
            </article>
          ))}
        </div>
      </section>

      <section className="card page-form-card">
        <h3>趋势数据</h3>
        <div className="inline-filter-form">
          <select value={metric} onChange={(event) => setMetric(event.target.value)}>
            <option value="NEW_USERS">新增用户</option>
            <option value="ADOPTION_POSTS">新增送养帖</option>
            <option value="FEEDING_ORDERS">新增喂养订单</option>
            <option value="RESCUE_CLUES">新增救助线索</option>
            <option value="COMPLAINTS">新增投诉工单</option>
          </select>
          <select value={days} onChange={(event) => setDays(event.target.value)}>
            <option value="7">近 7 天</option>
            <option value="14">近 14 天</option>
            <option value="30">近 30 天</option>
          </select>
        </div>

        <ul className="timeline">
          {trends.map((point) => (
            <li key={point.date}>
              <span>{point.date}</span>
              <p>{formatNumber(point.value)}</p>
            </li>
          ))}
        </ul>
      </section>
    </div>
  );
}

export default AdminOpsDashboardPage;
