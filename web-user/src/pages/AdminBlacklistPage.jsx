import { useEffect, useMemo, useState } from "react";
import Pagination from "../components/Pagination.jsx";
import {
  listAdminBlacklists,
  upsertAdminBlacklist,
  updateAdminBlacklistStatus
} from "../api/opsApi";
import { formatDateTime } from "../utils/format";

const PAGE_SIZE = 20;

function nowLocalDateTime() {
  const date = new Date();
  const pad = (value) => String(value).padStart(2, "0");
  return `${date.getFullYear()}-${pad(date.getMonth() + 1)}-${pad(date.getDate())}T${pad(date.getHours())}:${pad(date.getMinutes())}`;
}

function AdminBlacklistPage() {
  const [draft, setDraft] = useState({
    subjectType: "",
    subjectValue: "",
    scopeType: "",
    scopeValue: "",
    status: "",
    keyword: ""
  });
  const [filters, setFilters] = useState(draft);
  const [page, setPage] = useState(1);
  const [result, setResult] = useState({ items: [], total: 0, page: 1, pageSize: PAGE_SIZE });
  const [loading, setLoading] = useState(false);
  const [notice, setNotice] = useState("");

  const [form, setForm] = useState({
    blacklistId: "0",
    subjectType: "USER_ID",
    subjectValue: "",
    scopeType: "ACTION",
    scopeValue: "ADOPTION_POST_CREATE",
    actionMode: "BLOCK",
    reasonCode: "RISK_MANUAL_BLOCK",
    reasonNote: "",
    startAt: nowLocalDateTime(),
    endAt: "",
    status: "ACTIVE"
  });
  const [saving, setSaving] = useState(false);

  const queryParams = useMemo(() => ({ page, pageSize: PAGE_SIZE, ...filters }), [page, filters]);

  useEffect(() => {
    let cancelled = false;

    async function loadData() {
      setLoading(true);
      setNotice("");
      try {
        const data = await listAdminBlacklists(queryParams);
        if (!cancelled) {
          setResult(data);
        }
      } catch (err) {
        if (!cancelled) {
          setNotice(err.message || "加载黑名单失败");
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

  function onFormChange(event) {
    const { name, value } = event.target;
    setForm((prev) => ({ ...prev, [name]: value }));
  }

  async function onSaveRule(event) {
    event.preventDefault();
    setSaving(true);
    setNotice("");

    try {
      await upsertAdminBlacklist(Number(form.blacklistId || 0), {
        subjectType: form.subjectType,
        subjectValue: form.subjectValue.trim(),
        scopeType: form.scopeType,
        scopeValue: form.scopeValue.trim(),
        actionMode: form.actionMode,
        reasonCode: form.reasonCode.trim(),
        reasonNote: form.reasonNote.trim() || undefined,
        startAt: form.startAt,
        endAt: form.endAt || undefined,
        status: form.status
      });
      setNotice("黑名单规则已保存");
      setForm((prev) => ({ ...prev, blacklistId: "0", subjectValue: "", reasonNote: "" }));
      const refreshed = await listAdminBlacklists(queryParams);
      setResult(refreshed);
    } catch (err) {
      setNotice(err.message || "保存规则失败");
    } finally {
      setSaving(false);
    }
  }

  async function onSwitchStatus(item, targetStatus) {
    try {
      await updateAdminBlacklistStatus(item.blacklistId, {
        status: targetStatus,
        reasonNote: targetStatus === "DISABLED" ? "后台手动停用" : "后台手动启用"
      });
      setNotice(`规则 #${item.blacklistId} 已更新为 ${targetStatus}`);
      const refreshed = await listAdminBlacklists(queryParams);
      setResult(refreshed);
    } catch (err) {
      setNotice(err.message || "更新状态失败");
    }
  }

  function onEdit(item) {
    setForm({
      blacklistId: String(item.blacklistId),
      subjectType: item.subjectType,
      subjectValue: "",
      scopeType: item.scopeType,
      scopeValue: item.scopeValue,
      actionMode: item.actionMode,
      reasonCode: item.reasonCode,
      reasonNote: "",
      startAt: item.startAt ? item.startAt.slice(0, 16) : nowLocalDateTime(),
      endAt: item.endAt ? item.endAt.slice(0, 16) : "",
      status: item.status === "EXPIRED" ? "DISABLED" : item.status
    });
    setNotice(`已载入规则 #${item.blacklistId}，请补全 subjectValue 后保存`);
  }

  return (
    <div className="page-stack">
      <section className="card page-banner">
        <p className="eyebrow">Admin Risk</p>
        <h1>黑名单管理</h1>
        <p>支持按主体、范围、状态筛选；支持新增、编辑、启用/停用规则。</p>
      </section>

      <section className="card page-form-card">
        <form className="inline-filter-form" onSubmit={onFilterSubmit}>
          <select name="subjectType" value={draft.subjectType} onChange={onFilterChange}>
            <option value="">全部主体类型</option>
            <option value="USER_ID">USER_ID</option>
            <option value="MOBILE">MOBILE</option>
          </select>
          <input name="subjectValue" value={draft.subjectValue} onChange={onFilterChange} placeholder="主体值" />
          <select name="scopeType" value={draft.scopeType} onChange={onFilterChange}>
            <option value="">全部范围类型</option>
            <option value="GLOBAL">GLOBAL</option>
            <option value="MODULE">MODULE</option>
            <option value="ACTION">ACTION</option>
          </select>
          <input name="scopeValue" value={draft.scopeValue} onChange={onFilterChange} placeholder="范围值" />
          <select name="status" value={draft.status} onChange={onFilterChange}>
            <option value="">全部状态</option>
            <option value="ACTIVE">ACTIVE</option>
            <option value="DISABLED">DISABLED</option>
            <option value="EXPIRED">EXPIRED</option>
          </select>
          <input name="keyword" value={draft.keyword} onChange={onFilterChange} placeholder="关键词" />
          <button className="primary-btn" type="submit">
            查询
          </button>
        </form>

        {loading ? <p className="helper-text">加载中...</p> : null}
        {notice ? <p className="helper-text">{notice}</p> : null}

        <div className="list-stack">
          {result.items.map((item) => (
            <article key={item.blacklistId} className="list-card">
              <div className="list-card-main">
                <h3>#{item.blacklistId}</h3>
                <p className="helper-text">
                  主体：{item.subjectType} · {item.subjectValueMasked}
                </p>
                <p className="helper-text">
                  范围：{item.scopeType} · {item.scopeValue} · 模式：{item.actionMode}
                </p>
                <p className="helper-text">
                  原因：{item.reasonCode} · 状态：{item.status}
                </p>
                <p className="helper-text">
                  生效：{formatDateTime(item.startAt)} ~ {formatDateTime(item.endAt)}
                </p>
              </div>
              <div className="list-card-actions">
                <button className="secondary-btn" type="button" onClick={() => onEdit(item)}>
                  编辑
                </button>
                {item.status !== "ACTIVE" ? (
                  <button className="secondary-btn" type="button" onClick={() => onSwitchStatus(item, "ACTIVE")}>
                    启用
                  </button>
                ) : (
                  <button className="secondary-btn" type="button" onClick={() => onSwitchStatus(item, "DISABLED")}>
                    停用
                  </button>
                )}
              </div>
            </article>
          ))}
        </div>

        {!loading && result.items.length === 0 ? <p className="helper-text">暂无规则</p> : null}

        <Pagination
          page={result.page || page}
          pageSize={result.pageSize || PAGE_SIZE}
          total={result.total || 0}
          onChange={setPage}
        />
      </section>

      <section className="card page-form-card">
        <h3>{form.blacklistId !== "0" ? `编辑规则 #${form.blacklistId}` : "新增规则"}</h3>
        <form className="stack-form" onSubmit={onSaveRule}>
          <div className="form-grid-two">
            <label>
              主体类型
              <select name="subjectType" value={form.subjectType} onChange={onFormChange} required>
                <option value="USER_ID">USER_ID</option>
                <option value="MOBILE">MOBILE</option>
              </select>
            </label>
            <label>
              主体值
              <input name="subjectValue" value={form.subjectValue} onChange={onFormChange} maxLength={128} required />
            </label>
          </div>

          <div className="form-grid-two">
            <label>
              范围类型
              <select name="scopeType" value={form.scopeType} onChange={onFormChange} required>
                <option value="GLOBAL">GLOBAL</option>
                <option value="MODULE">MODULE</option>
                <option value="ACTION">ACTION</option>
              </select>
            </label>
            <label>
              范围值
              <input name="scopeValue" value={form.scopeValue} onChange={onFormChange} maxLength={64} required />
            </label>
          </div>

          <div className="form-grid-two">
            <label>
              动作模式
              <select name="actionMode" value={form.actionMode} onChange={onFormChange} required>
                <option value="BLOCK">BLOCK</option>
                <option value="REVIEW_ONLY">REVIEW_ONLY</option>
                <option value="LIMIT">LIMIT</option>
              </select>
            </label>
            <label>
              规则状态
              <select name="status" value={form.status} onChange={onFormChange} required>
                <option value="ACTIVE">ACTIVE</option>
                <option value="DISABLED">DISABLED</option>
              </select>
            </label>
          </div>

          <label>
            原因代码
            <input name="reasonCode" value={form.reasonCode} onChange={onFormChange} maxLength={64} required />
          </label>

          <label>
            原因备注
            <input name="reasonNote" value={form.reasonNote} onChange={onFormChange} maxLength={255} />
          </label>

          <div className="form-grid-two">
            <label>
              生效开始
              <input type="datetime-local" name="startAt" value={form.startAt} onChange={onFormChange} required />
            </label>
            <label>
              生效结束（可选）
              <input type="datetime-local" name="endAt" value={form.endAt} onChange={onFormChange} />
            </label>
          </div>

          <button className="primary-btn" type="submit" disabled={saving}>
            {saving ? "保存中..." : "保存规则"}
          </button>
        </form>
      </section>
    </div>
  );
}

export default AdminBlacklistPage;
