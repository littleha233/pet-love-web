import { useEffect, useMemo, useState } from "react";
import Pagination from "../components/Pagination.jsx";
import { listAdminCityFeatures, upsertAdminCityFeature } from "../api/opsApi";
import { formatDateTime } from "../utils/format";

const PAGE_SIZE = 20;

function AdminCityFeaturePage() {
  const [draft, setDraft] = useState({ cityCode: "", featureKey: "" });
  const [filters, setFilters] = useState(draft);
  const [page, setPage] = useState(1);
  const [result, setResult] = useState({ items: [], total: 0, page: 1, pageSize: PAGE_SIZE });
  const [loading, setLoading] = useState(false);
  const [notice, setNotice] = useState("");

  const [form, setForm] = useState({
    switchId: "0",
    cityCode: "",
    cityName: "",
    featureKey: "ADOPTION",
    isEnabled: true,
    allowRead: true,
    allowWrite: true,
    noticeText: "",
    effectiveFrom: "",
    effectiveTo: ""
  });
  const [saving, setSaving] = useState(false);

  const queryParams = useMemo(() => ({ page, pageSize: PAGE_SIZE, ...filters }), [page, filters]);

  useEffect(() => {
    let cancelled = false;

    async function loadData() {
      setLoading(true);
      setNotice("");
      try {
        const data = await listAdminCityFeatures(queryParams);
        if (!cancelled) {
          setResult(data);
        }
      } catch (err) {
        if (!cancelled) {
          setNotice(err.message || "加载城市开关失败");
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
    const { name, value, type, checked } = event.target;
    setForm((prev) => ({ ...prev, [name]: type === "checkbox" ? checked : value }));
  }

  async function onSave(event) {
    event.preventDefault();
    setSaving(true);
    setNotice("");
    try {
      await upsertAdminCityFeature(Number(form.switchId || 0), {
        cityCode: form.cityCode.trim(),
        cityName: form.cityName.trim(),
        featureKey: form.featureKey,
        isEnabled: Boolean(form.isEnabled),
        allowRead: Boolean(form.allowRead),
        allowWrite: Boolean(form.allowWrite),
        noticeText: form.noticeText.trim() || undefined,
        effectiveFrom: form.effectiveFrom || undefined,
        effectiveTo: form.effectiveTo || undefined
      });
      setNotice("城市功能开关已保存");
      const refreshed = await listAdminCityFeatures(queryParams);
      setResult(refreshed);
      setForm((prev) => ({ ...prev, switchId: "0" }));
    } catch (err) {
      setNotice(err.message || "保存城市开关失败");
    } finally {
      setSaving(false);
    }
  }

  function onEdit(item) {
    setForm({
      switchId: String(item.switchId),
      cityCode: item.cityCode || "",
      cityName: item.cityName || "",
      featureKey: item.featureKey || "ADOPTION",
      isEnabled: Boolean(item.isEnabled),
      allowRead: Boolean(item.allowRead),
      allowWrite: Boolean(item.allowWrite),
      noticeText: item.noticeText || "",
      effectiveFrom: item.effectiveFrom ? item.effectiveFrom.slice(0, 16) : "",
      effectiveTo: item.effectiveTo ? item.effectiveTo.slice(0, 16) : ""
    });
    setNotice(`已载入开关 #${item.switchId}`);
  }

  return (
    <div className="page-stack">
      <section className="card page-banner">
        <p className="eyebrow">Admin Ops</p>
        <h1>城市功能开关</h1>
        <p>按城市和功能进行读写控制，并设置前台提示文案与生效时间。</p>
      </section>

      <section className="card page-form-card">
        <form className="inline-filter-form" onSubmit={onFilterSubmit}>
          <input name="cityCode" value={draft.cityCode} onChange={onFilterChange} placeholder="城市编码" />
          <select name="featureKey" value={draft.featureKey} onChange={onFilterChange}>
            <option value="">全部功能</option>
            <option value="ADOPTION">ADOPTION</option>
            <option value="FEEDING">FEEDING</option>
            <option value="RESCUE_GUIDE">RESCUE_GUIDE</option>
            <option value="RESCUE_RESOURCE">RESCUE_RESOURCE</option>
            <option value="RESCUE_CLUE_SUBMIT">RESCUE_CLUE_SUBMIT</option>
          </select>
          <button className="primary-btn" type="submit">
            查询
          </button>
        </form>

        {loading ? <p className="helper-text">加载中...</p> : null}
        {notice ? <p className="helper-text">{notice}</p> : null}

        <div className="list-stack">
          {result.items.map((item) => (
            <article key={item.switchId} className="list-card">
              <div className="list-card-main">
                <h3>#{item.switchId}</h3>
                <p className="helper-text">
                  城市：{item.cityName}（{item.cityCode}） · 功能：{item.featureKey}
                </p>
                <p className="helper-text">
                  总开关：{item.isEnabled ? "开启" : "关闭"} · 浏览：{item.allowRead ? "允许" : "禁用"} · 写入：{item.allowWrite ? "允许" : "禁用"}
                </p>
                <p className="helper-text">提示文案：{item.noticeText || "-"}</p>
                <p className="helper-text">
                  生效：{formatDateTime(item.effectiveFrom)} ~ {formatDateTime(item.effectiveTo)}
                </p>
              </div>
              <div className="list-card-actions">
                <button className="secondary-btn" type="button" onClick={() => onEdit(item)}>
                  编辑
                </button>
              </div>
            </article>
          ))}
        </div>

        {!loading && result.items.length === 0 ? <p className="helper-text">暂无配置</p> : null}

        <Pagination
          page={result.page || page}
          pageSize={result.pageSize || PAGE_SIZE}
          total={result.total || 0}
          onChange={setPage}
        />
      </section>

      <section className="card page-form-card">
        <h3>{form.switchId !== "0" ? `编辑开关 #${form.switchId}` : "新增开关"}</h3>
        <form className="stack-form" onSubmit={onSave}>
          <div className="form-grid-two">
            <label>
              城市编码
              <input name="cityCode" value={form.cityCode} onChange={onFormChange} maxLength={32} required />
            </label>
            <label>
              城市名称
              <input name="cityName" value={form.cityName} onChange={onFormChange} maxLength={64} required />
            </label>
          </div>

          <label>
            功能键
            <select name="featureKey" value={form.featureKey} onChange={onFormChange} required>
              <option value="ADOPTION">ADOPTION</option>
              <option value="FEEDING">FEEDING</option>
              <option value="RESCUE_GUIDE">RESCUE_GUIDE</option>
              <option value="RESCUE_RESOURCE">RESCUE_RESOURCE</option>
              <option value="RESCUE_CLUE_SUBMIT">RESCUE_CLUE_SUBMIT</option>
            </select>
          </label>

          <div className="form-grid-two">
            <label>
              <input type="checkbox" name="isEnabled" checked={form.isEnabled} onChange={onFormChange} />
              启用总开关
            </label>
            <label>
              <input type="checkbox" name="allowRead" checked={form.allowRead} onChange={onFormChange} />
              允许浏览
            </label>
          </div>

          <label>
            <input type="checkbox" name="allowWrite" checked={form.allowWrite} onChange={onFormChange} />
            允许写入
          </label>

          <label>
            前台提示文案
            <input name="noticeText" value={form.noticeText} onChange={onFormChange} maxLength={255} />
          </label>

          <div className="form-grid-two">
            <label>
              生效开始（可选）
              <input type="datetime-local" name="effectiveFrom" value={form.effectiveFrom} onChange={onFormChange} />
            </label>
            <label>
              生效结束（可选）
              <input type="datetime-local" name="effectiveTo" value={form.effectiveTo} onChange={onFormChange} />
            </label>
          </div>

          <button className="primary-btn" type="submit" disabled={saving}>
            {saving ? "保存中..." : "保存开关"}
          </button>
        </form>
      </section>
    </div>
  );
}

export default AdminCityFeaturePage;
