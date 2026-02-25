import { useEffect, useMemo, useState } from "react";
import Pagination from "../components/Pagination.jsx";
import { getAdminOpsAuditLogDetail, listAdminOpsAuditLogs } from "../api/opsApi";
import { formatDateTime } from "../utils/format";

const PAGE_SIZE = 20;

function formatJsonBlock(value) {
  if (value === null || value === undefined || value === "") {
    return "-";
  }
  if (typeof value === "string") {
    return value;
  }
  try {
    return JSON.stringify(value, null, 2);
  } catch (err) {
    return String(value);
  }
}

function AdminAuditLogSearchPage() {
  const [draft, setDraft] = useState({
    operatorAdminId: "",
    moduleName: "",
    actionName: "",
    targetType: "",
    targetId: "",
    keyword: "",
    dateFrom: "",
    dateTo: ""
  });
  const [filters, setFilters] = useState(draft);
  const [page, setPage] = useState(1);
  const [result, setResult] = useState({ items: [], total: 0, page: 1, pageSize: PAGE_SIZE });
  const [loading, setLoading] = useState(false);
  const [notice, setNotice] = useState("");
  const [detail, setDetail] = useState(null);
  const [loadingDetail, setLoadingDetail] = useState(false);

  const queryParams = useMemo(() => ({ page, pageSize: PAGE_SIZE, ...filters }), [page, filters]);

  useEffect(() => {
    let cancelled = false;

    async function loadData() {
      setLoading(true);
      setNotice("");
      try {
        const data = await listAdminOpsAuditLogs(queryParams);
        if (!cancelled) {
          setResult(data);
        }
      } catch (err) {
        if (!cancelled) {
          setNotice(err.message || "加载审计日志失败");
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
  }, [queryParams]);

  function onFilterChange(event) {
    const { name, value } = event.target;
    setDraft((prev) => ({ ...prev, [name]: value }));
  }

  function onFilterSubmit(event) {
    event.preventDefault();
    setPage(1);
    setFilters({ ...draft });
  }

  async function onViewDetail(auditLogId) {
    setLoadingDetail(true);
    setNotice("");
    try {
      const data = await getAdminOpsAuditLogDetail(auditLogId);
      setDetail(data);
    } catch (err) {
      setNotice(err.message || "加载日志详情失败");
    } finally {
      setLoadingDetail(false);
    }
  }

  return (
    <div className="page-stack">
      <section className="card page-banner">
        <p className="eyebrow">Admin Ops</p>
        <h1>审计日志检索</h1>
        <p>支持按模块、动作、操作人、目标与时间检索后台操作记录。</p>
      </section>

      <section className="card page-form-card">
        <form className="inline-filter-form" onSubmit={onFilterSubmit}>
          <input
            name="operatorAdminId"
            value={draft.operatorAdminId}
            onChange={onFilterChange}
            placeholder="操作人ID"
          />
          <input name="moduleName" value={draft.moduleName} onChange={onFilterChange} placeholder="模块名" />
          <input name="actionName" value={draft.actionName} onChange={onFilterChange} placeholder="动作名" />
          <input name="targetType" value={draft.targetType} onChange={onFilterChange} placeholder="目标类型" />
          <input name="targetId" value={draft.targetId} onChange={onFilterChange} placeholder="目标ID" />
          <input name="keyword" value={draft.keyword} onChange={onFilterChange} placeholder="关键词" />
          <input name="dateFrom" type="date" value={draft.dateFrom} onChange={onFilterChange} />
          <input name="dateTo" type="date" value={draft.dateTo} onChange={onFilterChange} />
          <button className="primary-btn" type="submit">
            查询
          </button>
        </form>

        {loading ? <p className="helper-text">加载中...</p> : null}
        {notice ? <p className="helper-text">{notice}</p> : null}

        <div className="list-stack">
          {result.items.map((item) => (
            <article key={item.auditLogId} className="list-card">
              <div className="list-card-main">
                <h3>#{item.auditLogId}</h3>
                <p className="helper-text">
                  操作人：{item.operatorAdminName || item.operatorAdminId} · 时间：{formatDateTime(item.createdAt)}
                </p>
                <p className="helper-text">
                  模块：{item.moduleName} · 动作：{item.actionName}
                </p>
                <p className="helper-text">
                  目标：{item.targetType || "-"} / {item.targetId || "-"}
                </p>
                <p className="helper-text">摘要：{item.summary || "-"}</p>
              </div>
              <div className="list-card-actions">
                <button className="secondary-btn" type="button" onClick={() => onViewDetail(item.auditLogId)}>
                  查看详情
                </button>
              </div>
            </article>
          ))}
        </div>

        {!loading && result.items.length === 0 ? <p className="helper-text">暂无日志</p> : null}

        <Pagination
          page={result.page || page}
          pageSize={result.pageSize || PAGE_SIZE}
          total={result.total || 0}
          onChange={setPage}
        />
      </section>

      {detail ? (
        <section className="card page-form-card">
          <h3>日志详情 #{detail.auditLogId}</h3>
          {loadingDetail ? <p className="helper-text">详情加载中...</p> : null}
          <p className="helper-text">
            操作人：{detail.operatorAdminName || detail.operatorAdminId} · 时间：{formatDateTime(detail.createdAt)}
          </p>
          <p className="helper-text">
            模块：{detail.moduleName} · 动作：{detail.actionName} · 目标：{detail.targetType || "-"} / {detail.targetId || "-"}
          </p>

          <label>
            Before Snapshot
            <textarea readOnly rows={8} value={formatJsonBlock(detail.beforeSnapshot)} />
          </label>

          <label>
            After Snapshot
            <textarea readOnly rows={8} value={formatJsonBlock(detail.afterSnapshot)} />
          </label>

          <label>
            Extra Data
            <textarea readOnly rows={5} value={formatJsonBlock(detail.extraData)} />
          </label>
        </section>
      ) : null}
    </div>
  );
}

export default AdminAuditLogSearchPage;
