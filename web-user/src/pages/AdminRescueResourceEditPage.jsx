import { useEffect, useMemo, useState } from "react";
import { Link, useNavigate, useParams } from "react-router-dom";
import RescueResourceStatusTag from "../components/RescueResourceStatusTag.jsx";
import {
  activateAdminRescueResource,
  createAdminRescueResource,
  getAdminRescueResourceDetail,
  offlineAdminRescueResource,
  pauseAdminRescueResource,
  upsertAdminRescueResource
} from "../api/rescueApi";
import { formatDateTime } from "../utils/format";

const EMPTY_FORM = {
  resourceType: "ANIMAL_HOSPITAL",
  name: "",
  cityCode: "",
  cityName: "",
  districtName: "",
  address: "",
  contactPhone: "",
  contactWechat: "",
  contactOther: "",
  serviceHours: "",
  serviceScope: "",
  acceptPetTypesText: "CAT,DOG",
  capabilityTagsText: "",
  description: "",
  sourceUrl: "",
  verifiedAt: "",
  sortOrder: "0",
  status: "DRAFT"
};

function AdminRescueResourceEditPage() {
  const { resourceId } = useParams();
  const navigate = useNavigate();
  const isCreate = useMemo(() => resourceId === "new", [resourceId]);

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
        const data = await getAdminRescueResourceDetail(resourceId);
        if (!cancelled) {
          setDetail(data);
          setForm({
            resourceType: data.resourceType || "ANIMAL_HOSPITAL",
            name: data.name || "",
            cityCode: data.cityCode || "",
            cityName: data.cityName || "",
            districtName: data.districtName || "",
            address: data.address || "",
            contactPhone: data.contactPhone || "",
            contactWechat: data.contactWechat || "",
            contactOther: data.contactOther || "",
            serviceHours: data.serviceHours || "",
            serviceScope: data.serviceScope || "",
            acceptPetTypesText: (data.acceptPetTypes || []).join(","),
            capabilityTagsText: (data.capabilityTags || []).join(","),
            description: data.description || "",
            sourceUrl: data.sourceUrl || "",
            verifiedAt: data.verifiedAt ? String(data.verifiedAt).slice(0, 19) : "",
            sortOrder: String(data.sortOrder ?? 0),
            status: data.status || "DRAFT"
          });
        }
      } catch (err) {
        if (!cancelled) {
          setNotice(err.message || "加载资源详情失败");
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
  }, [resourceId, isCreate]);

  function onChange(event) {
    const { name, value } = event.target;
    setForm((prev) => ({ ...prev, [name]: value }));
  }

  async function onSave(event) {
    event.preventDefault();
    setSaving(true);
    setNotice("");

    const payload = {
      resourceType: form.resourceType,
      name: form.name,
      cityCode: form.cityCode,
      cityName: form.cityName,
      districtName: form.districtName || undefined,
      address: form.address || undefined,
      contactPhone: form.contactPhone || undefined,
      contactWechat: form.contactWechat || undefined,
      contactOther: form.contactOther || undefined,
      serviceHours: form.serviceHours || undefined,
      serviceScope: form.serviceScope || undefined,
      acceptPetTypes: form.acceptPetTypesText
        .split(",")
        .map((item) => item.trim())
        .filter(Boolean),
      capabilityTags: form.capabilityTagsText
        .split(",")
        .map((item) => item.trim())
        .filter(Boolean),
      description: form.description || undefined,
      sourceUrl: form.sourceUrl || undefined,
      verifiedAt: form.verifiedAt || undefined,
      sortOrder: Number(form.sortOrder || "0"),
      status: form.status || undefined
    };

    try {
      const saved = isCreate
        ? await createAdminRescueResource(payload)
        : await upsertAdminRescueResource(resourceId, payload);
      setNotice("保存成功");
      if (isCreate) {
        navigate(`/admin/rescue/resources/${saved.resourceId}`);
      } else {
        setDetail(saved);
      }
    } catch (err) {
      setNotice(err.message || "保存失败");
    } finally {
      setSaving(false);
    }
  }

  async function quickAction(action) {
    if (isCreate) {
      return;
    }

    try {
      if (action === "activate") {
        await activateAdminRescueResource(resourceId);
      }
      if (action === "pause") {
        await pauseAdminRescueResource(resourceId);
      }
      if (action === "offline") {
        await offlineAdminRescueResource(resourceId);
      }
      setNotice("状态更新成功");
      const refreshed = await getAdminRescueResourceDetail(resourceId);
      setDetail(refreshed);
      setForm((prev) => ({ ...prev, status: refreshed.status || prev.status }));
    } catch (err) {
      setNotice(err.message || "状态更新失败");
    }
  }

  return (
    <div className="page-stack">
      <section className="card page-banner">
        <p className="eyebrow">Admin 资源编辑</p>
        <h1>{isCreate ? "新建救助资源" : `编辑资源 #${resourceId}`}</h1>
        <p>
          <Link to="/admin/rescue/resources">返回资源列表</Link>
        </p>
      </section>

      <section className="card page-form-card">
        {loading ? <p className="helper-text">加载中...</p> : null}
        {notice ? <p className="helper-text notice-text">{notice}</p> : null}

        {detail ? (
          <div className="tag-row">
            <RescueResourceStatusTag status={detail.status} />
            <span className="soft-tag">核验：{formatDateTime(detail.verifiedAt)}</span>
            <span className="soft-tag">更新时间：{formatDateTime(detail.updatedAt)}</span>
          </div>
        ) : null}

        <form className="stack-form" onSubmit={onSave}>
          <div className="form-grid-two">
            <label>
              资源类型*
              <select name="resourceType" value={form.resourceType} onChange={onChange} required>
                <option value="ANIMAL_HOSPITAL">ANIMAL_HOSPITAL</option>
                <option value="SHELTER">SHELTER</option>
                <option value="VOLUNTEER_GROUP">VOLUNTEER_GROUP</option>
                <option value="OFFICIAL_CHANNEL">OFFICIAL_CHANNEL</option>
                <option value="NGO">NGO</option>
              </select>
            </label>
            <label>
              名称*
              <input name="name" value={form.name} onChange={onChange} maxLength={200} required />
            </label>
          </div>

          <div className="form-grid-two">
            <label>
              城市编码*
              <input name="cityCode" value={form.cityCode} onChange={onChange} maxLength={32} required />
            </label>
            <label>
              城市名称*
              <input name="cityName" value={form.cityName} onChange={onChange} maxLength={64} required />
            </label>
          </div>

          <div className="form-grid-two">
            <label>
              区域
              <input name="districtName" value={form.districtName} onChange={onChange} maxLength={64} />
            </label>
            <label>
              地址
              <input name="address" value={form.address} onChange={onChange} maxLength={255} />
            </label>
          </div>

          <div className="form-grid-two">
            <label>
              联系电话
              <input name="contactPhone" value={form.contactPhone} onChange={onChange} maxLength={64} />
            </label>
            <label>
              微信
              <input name="contactWechat" value={form.contactWechat} onChange={onChange} maxLength={64} />
            </label>
          </div>

          <label>
            其他联系方式
            <input name="contactOther" value={form.contactOther} onChange={onChange} maxLength={255} />
          </label>

          <div className="form-grid-two">
            <label>
              服务时间
              <input name="serviceHours" value={form.serviceHours} onChange={onChange} maxLength={128} />
            </label>
            <label>
              服务范围
              <input name="serviceScope" value={form.serviceScope} onChange={onChange} maxLength={255} />
            </label>
          </div>

          <div className="form-grid-two">
            <label>
              可接收宠物类型（逗号分隔）
              <input name="acceptPetTypesText" value={form.acceptPetTypesText} onChange={onChange} placeholder="CAT,DOG" />
            </label>
            <label>
              能力标签（逗号分隔）
              <input name="capabilityTagsText" value={form.capabilityTagsText} onChange={onChange} />
            </label>
          </div>

          <label>
            描述
            <textarea name="description" value={form.description} onChange={onChange} rows={4} />
          </label>

          <div className="form-grid-two">
            <label>
              来源链接
              <input name="sourceUrl" value={form.sourceUrl} onChange={onChange} maxLength={500} />
            </label>
            <label>
              核验时间（YYYY-MM-DD 或 ISO）
              <input name="verifiedAt" value={form.verifiedAt} onChange={onChange} placeholder="2026-02-24" />
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
                <option value="ACTIVE">ACTIVE</option>
                <option value="PAUSED">PAUSED</option>
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
            <button className="secondary-btn" type="button" onClick={() => quickAction("activate")}>
              启用
            </button>
            <button className="secondary-btn" type="button" onClick={() => quickAction("pause")}>
              暂停
            </button>
            <button className="secondary-btn" type="button" onClick={() => quickAction("offline")}>
              下线
            </button>
          </div>
        ) : null}
      </section>
    </div>
  );
}

export default AdminRescueResourceEditPage;
