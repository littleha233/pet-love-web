import { useEffect, useMemo, useState } from "react";
import { Link, useNavigate, useParams } from "react-router-dom";
import RescueGuideStatusTag from "../components/RescueGuideStatusTag.jsx";
import {
  createAdminRescueGuide,
  getAdminRescueGuideDetail,
  offlineAdminRescueGuide,
  publishAdminRescueGuide,
  upsertAdminRescueGuide
} from "../api/rescueApi";
import { formatDateTime } from "../utils/format";

const EMPTY_FORM = {
  scenarioCode: "FOUND_STRAY_CAT",
  title: "",
  summary: "",
  contentMd: "",
  cityCode: "",
  tagsText: "",
  sortOrder: "0",
  status: "DRAFT"
};

function AdminRescueGuideEditPage() {
  const { guideId } = useParams();
  const navigate = useNavigate();
  const isCreate = useMemo(() => guideId === "new", [guideId]);

  const [detail, setDetail] = useState(null);
  const [form, setForm] = useState(EMPTY_FORM);
  const [loading, setLoading] = useState(false);
  const [saving, setSaving] = useState(false);
  const [notice, setNotice] = useState("");

  useEffect(() => {
    if (isCreate) {
      setDetail(null);
      setForm(EMPTY_FORM);
      return;
    }

    let cancelled = false;

    async function loadData() {
      setLoading(true);
      setNotice("");
      try {
        const data = await getAdminRescueGuideDetail(guideId);
        if (!cancelled) {
          setDetail(data);
          setForm({
            scenarioCode: data.scenarioCode || "FOUND_STRAY_CAT",
            title: data.title || "",
            summary: data.summary || "",
            contentMd: data.contentMd || "",
            cityCode: data.cityCode || "",
            tagsText: (data.tags || []).join(","),
            sortOrder: String(data.sortOrder ?? 0),
            status: data.status || "DRAFT"
          });
        }
      } catch (err) {
        if (!cancelled) {
          setNotice(err.message || "加载指引详情失败");
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
  }, [guideId, isCreate]);

  function onChange(event) {
    const { name, value } = event.target;
    setForm((prev) => ({ ...prev, [name]: value }));
  }

  async function onSave(event) {
    event.preventDefault();
    setSaving(true);
    setNotice("");

    const payload = {
      scenarioCode: form.scenarioCode,
      title: form.title,
      summary: form.summary || undefined,
      contentMd: form.contentMd,
      cityCode: form.cityCode || undefined,
      tags: form.tagsText
        .split(",")
        .map((item) => item.trim())
        .filter(Boolean),
      sortOrder: Number(form.sortOrder || "0"),
      status: form.status || undefined
    };

    try {
      const saved = isCreate
        ? await createAdminRescueGuide(payload)
        : await upsertAdminRescueGuide(guideId, payload);
      setNotice("保存成功");
      if (isCreate) {
        navigate(`/admin/rescue/guides/${saved.guideId}`);
      } else {
        setDetail(saved);
      }
    } catch (err) {
      setNotice(err.message || "保存失败");
    } finally {
      setSaving(false);
    }
  }

  async function onPublish() {
    if (isCreate) {
      return;
    }
    try {
      await publishAdminRescueGuide(guideId);
      setNotice("发布成功");
      const refreshed = await getAdminRescueGuideDetail(guideId);
      setDetail(refreshed);
      setForm((prev) => ({ ...prev, status: refreshed.status || prev.status }));
    } catch (err) {
      setNotice(err.message || "发布失败");
    }
  }

  async function onOffline() {
    if (isCreate) {
      return;
    }
    try {
      await offlineAdminRescueGuide(guideId);
      setNotice("已下线");
      const refreshed = await getAdminRescueGuideDetail(guideId);
      setDetail(refreshed);
      setForm((prev) => ({ ...prev, status: refreshed.status || prev.status }));
    } catch (err) {
      setNotice(err.message || "下线失败");
    }
  }

  return (
    <div className="page-stack">
      <section className="card page-banner">
        <p className="eyebrow">Admin 指引编辑</p>
        <h1>{isCreate ? "新建救助指引" : `编辑指引 #${guideId}`}</h1>
        <p>
          <Link to="/admin/rescue/guides">返回指引列表</Link>
        </p>
      </section>

      <section className="card page-form-card">
        {loading ? <p className="helper-text">加载中...</p> : null}
        {notice ? <p className="helper-text">{notice}</p> : null}

        {detail ? (
          <div className="tag-row">
            <RescueGuideStatusTag status={detail.status} />
            <span className="soft-tag">版本：{detail.version}</span>
            <span className="soft-tag">发布时间：{formatDateTime(detail.publishedAt)}</span>
          </div>
        ) : null}

        <form className="stack-form" onSubmit={onSave}>
          <label>
            场景编码*
            <select name="scenarioCode" value={form.scenarioCode} onChange={onChange} required>
              <option value="FOUND_STRAY_CAT">FOUND_STRAY_CAT</option>
              <option value="FOUND_STRAY_DOG">FOUND_STRAY_DOG</option>
              <option value="INJURED_CAT">INJURED_CAT</option>
              <option value="INJURED_DOG">INJURED_DOG</option>
              <option value="ABANDONED_KITTENS">ABANDONED_KITTENS</option>
              <option value="ABANDONED_PUPPIES">ABANDONED_PUPPIES</option>
              <option value="EMERGENCY_TRANSPORT">EMERGENCY_TRANSPORT</option>
            </select>
          </label>

          <label>
            标题*
            <input name="title" value={form.title} onChange={onChange} maxLength={200} required />
          </label>

          <label>
            摘要
            <textarea name="summary" value={form.summary} onChange={onChange} maxLength={500} rows={2} />
          </label>

          <label>
            Markdown 内容*
            <textarea name="contentMd" value={form.contentMd} onChange={onChange} rows={14} required />
          </label>

          <div className="form-grid-two">
            <label>
              城市编码（留空=全国）
              <input name="cityCode" value={form.cityCode} onChange={onChange} maxLength={32} />
            </label>
            <label>
              标签（逗号分隔）
              <input name="tagsText" value={form.tagsText} onChange={onChange} placeholder="新手,紧急情况" />
            </label>
          </div>

          <div className="form-grid-two">
            <label>
              排序值
              <input name="sortOrder" value={form.sortOrder} onChange={onChange} type="number" />
            </label>
            <label>
              状态
              <select name="status" value={form.status} onChange={onChange}>
                <option value="DRAFT">DRAFT</option>
                <option value="PUBLISHED">PUBLISHED</option>
                <option value="OFFLINE">OFFLINE</option>
              </select>
            </label>
          </div>

          <button className="primary-btn" type="submit" disabled={saving}>
            {saving ? "保存中..." : "保存"}
          </button>
        </form>

        {!isCreate ? (
          <div className="action-row" style={{ marginTop: "12px" }}>
            <button className="secondary-btn" type="button" onClick={onPublish}>
              发布
            </button>
            <button className="secondary-btn" type="button" onClick={onOffline}>
              下线
            </button>
          </div>
        ) : null}
      </section>
    </div>
  );
}

export default AdminRescueGuideEditPage;
