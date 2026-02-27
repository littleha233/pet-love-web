import { useEffect, useState } from "react";
import { getMyFeedingProviderProfile, upsertMyFeedingProviderProfile } from "../api/feedingApi";

function splitCsv(value) {
  if (!value) {
    return [];
  }
  return value
    .split(",")
    .map((item) => item.trim())
    .filter(Boolean);
}

function FeedingProviderCenterPage() {
  const [loading, setLoading] = useState(false);
  const [saving, setSaving] = useState(false);
  const [message, setMessage] = useState("");
  const [form, setForm] = useState({
    status: "DRAFT",
    displayName: "",
    headline: "",
    intro: "",
    serviceCityCode: "",
    serviceCityName: "",
    serviceDistricts: "",
    servicePetTypes: "CAT",
    serviceItemTags: "FEED,WATER,PHOTO_REPORT",
    basePricePerVisit: "",
    experienceYears: "",
    maxOrdersPerDay: "",
    acceptNotes: ""
  });

  async function loadProfile() {
    setLoading(true);
    setMessage("");
    try {
      const data = await getMyFeedingProviderProfile();
      setForm({
        status: data.status || (data.providerProfileId ? "ACTIVE" : "DRAFT"),
        displayName: data.displayName || "",
        headline: data.headline || "",
        intro: data.intro || "",
        serviceCityCode: data.serviceCityCode || "",
        serviceCityName: data.serviceCityName || "",
        serviceDistricts: (data.serviceDistricts || []).join(","),
        servicePetTypes: (data.servicePetTypes || []).join(",") || "CAT",
        serviceItemTags: (data.serviceItemTags || []).join(",") || "FEED,WATER,PHOTO_REPORT",
        basePricePerVisit: data.basePricePerVisit || "",
        experienceYears: data.experienceYears ?? "",
        maxOrdersPerDay: data.maxOrdersPerDay ?? "",
        acceptNotes: data.acceptNotes || ""
      });
    } catch (err) {
      setMessage(err.message || "加载资料失败");
    } finally {
      setLoading(false);
    }
  }

  useEffect(() => {
    loadProfile();
  }, []);

  function onChange(event) {
    const { name, value } = event.target;
    setForm((prev) => ({ ...prev, [name]: value }));
  }

  async function onSubmit(event) {
    event.preventDefault();
    setSaving(true);
    setMessage("");
    try {
      await upsertMyFeedingProviderProfile({
        status: form.status,
        displayName: form.displayName || undefined,
        headline: form.headline || undefined,
        intro: form.intro || undefined,
        serviceCityCode: form.serviceCityCode,
        serviceCityName: form.serviceCityName,
        serviceDistricts: splitCsv(form.serviceDistricts),
        servicePetTypes: splitCsv(form.servicePetTypes),
        serviceItemTags: splitCsv(form.serviceItemTags),
        basePricePerVisit: form.basePricePerVisit ? Number(form.basePricePerVisit) : undefined,
        experienceYears: form.experienceYears ? Number(form.experienceYears) : undefined,
        maxOrdersPerDay: form.maxOrdersPerDay ? Number(form.maxOrdersPerDay) : undefined,
        acceptNotes: form.acceptNotes || undefined
      });
      setMessage("资料保存成功");
      await loadProfile();
    } catch (err) {
      setMessage(err.message || "保存失败");
    } finally {
      setSaving(false);
    }
  }

  return (
    <div className="page-stack">
      <section className="card page-banner">
        <p className="eyebrow">服务者中心</p>
        <h1>上门喂养资料维护</h1>
        <p>仅服务者认证通过用户可维护和上架资料。</p>
      </section>

      <section className="card page-form-card">
        {loading ? <p className="helper-text">加载中...</p> : null}
        <form className="stack-form" onSubmit={onSubmit}>
          <div className="form-grid-two">
            <label>
              状态
              <select name="status" value={form.status} onChange={onChange}>
                <option value="DRAFT">DRAFT</option>
                <option value="ACTIVE">ACTIVE</option>
                <option value="PAUSED">PAUSED</option>
              </select>
            </label>
            <label>
              显示名称
              <input name="displayName" value={form.displayName} onChange={onChange} maxLength={64} />
            </label>
            <label className="full-row">
              标题
              <input name="headline" value={form.headline} onChange={onChange} maxLength={128} />
            </label>
            <label>
              服务城市编码*
              <input name="serviceCityCode" value={form.serviceCityCode} onChange={onChange} required />
            </label>
            <label>
              服务城市名称*
              <input name="serviceCityName" value={form.serviceCityName} onChange={onChange} required />
            </label>
            <label className="full-row">
              服务区域（逗号分隔）
              <input name="serviceDistricts" value={form.serviceDistricts} onChange={onChange} />
            </label>
            <label>
              服务宠物类型（逗号分隔）*
              <input name="servicePetTypes" value={form.servicePetTypes} onChange={onChange} required />
            </label>
            <label>
              服务项（逗号分隔）*
              <input name="serviceItemTags" value={form.serviceItemTags} onChange={onChange} required />
            </label>
            <label>
              参考价/次
              <input name="basePricePerVisit" type="number" min="0" step="0.01" value={form.basePricePerVisit} onChange={onChange} />
            </label>
            <label>
              从业年限
              <input name="experienceYears" type="number" min="0" max="50" value={form.experienceYears} onChange={onChange} />
            </label>
            <label>
              每日最大订单
              <input name="maxOrdersPerDay" type="number" min="1" max="50" value={form.maxOrdersPerDay} onChange={onChange} />
            </label>
            <label>
              接单说明
              <input name="acceptNotes" value={form.acceptNotes} onChange={onChange} maxLength={255} />
            </label>
            <label className="full-row">
              自我介绍
              <textarea name="intro" value={form.intro} onChange={onChange} rows={5} maxLength={2000} />
            </label>
          </div>

          {message ? <p className="helper-text notice-text">{message}</p> : null}
          <button className="primary-btn" type="submit" disabled={saving}>
            {saving ? "保存中..." : "保存资料"}
          </button>
        </form>
      </section>
    </div>
  );
}

export default FeedingProviderCenterPage;
