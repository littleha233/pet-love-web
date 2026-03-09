import { useState } from "react";
import { useNavigate } from "react-router-dom";
import { submitRescueClue, uploadRescueCluePhoto } from "../api/rescueApi";

const EMPTY_FORM = {
  cityCode: "",
  districtName: "",
  locationText: "",
  petType: "UNKNOWN",
  estimatedCount: "1",
  urgencyLevel: "MEDIUM",
  conditionTags: [],
  description: "",
  contactName: "",
  contactMobile: ""
};

const CITY_OPTIONS = [
  { code: "310100", name: "上海" },
  { code: "330100", name: "杭州" },
  { code: "320100", name: "南京" },
  { code: "440300", name: "深圳" }
];

const CONDITION_OPTIONS = [
  { value: "INJURED", label: "受伤" },
  { value: "BLEEDING", label: "出血" },
  { value: "WEAK", label: "虚弱" },
  { value: "TRAPPED", label: "受困" },
  { value: "KITTEN", label: "幼猫" },
  { value: "PUPPY", label: "幼犬" }
];

function RescueClueSubmitPage() {
  const navigate = useNavigate();
  const [form, setForm] = useState(EMPTY_FORM);
  const [photos, setPhotos] = useState([]);
  const [uploading, setUploading] = useState(false);
  const [submitting, setSubmitting] = useState(false);
  const [notice, setNotice] = useState("");

  function onChange(event) {
    const { name, value } = event.target;
    setForm((prev) => ({ ...prev, [name]: value }));
  }

  function onCityChange(event) {
    const cityCode = event.target.value;
    setForm((prev) => ({ ...prev, cityCode }));
  }

  function onToggleCondition(tagValue) {
    setForm((prev) => {
      const selected = prev.conditionTags.includes(tagValue);
      return {
        ...prev,
        conditionTags: selected
          ? prev.conditionTags.filter((item) => item !== tagValue)
          : [...prev.conditionTags, tagValue]
      };
    });
  }

  async function onSelectPhotos(event) {
    const files = Array.from(event.target.files || []);
    if (files.length === 0) {
      return;
    }
    const remain = 9 - photos.length;
    if (remain <= 0) {
      setNotice("最多上传 9 张图片");
      return;
    }

    setUploading(true);
    setNotice("");
    try {
      const uploadTargets = files.slice(0, remain);
      const uploaded = [];
      for (const file of uploadTargets) {
        const result = await uploadRescueCluePhoto(file);
        uploaded.push({ fileId: result.fileId, url: result.url, name: file.name });
      }
      setPhotos((prev) => [...prev, ...uploaded]);
      if (files.length > remain) {
        setNotice(`仅上传前 ${remain} 张，已达到上限 9 张`);
      }
    } catch (err) {
      setNotice(err.message || "图片上传失败");
    } finally {
      setUploading(false);
      event.target.value = "";
    }
  }

  function removePhoto(fileId) {
    setPhotos((prev) => prev.filter((item) => item.fileId !== fileId));
  }

  async function onSubmit(event) {
    event.preventDefault();
    setSubmitting(true);
    setNotice("");

    const selectedCity = CITY_OPTIONS.find((city) => city.code === form.cityCode);

    const payload = {
      cityCode: form.cityCode.trim(),
      cityName: selectedCity?.name || "",
      districtName: form.districtName.trim() || undefined,
      locationText: form.locationText.trim(),
      petType: form.petType || undefined,
      estimatedCount: form.estimatedCount.trim() ? Number(form.estimatedCount.trim()) : undefined,
      urgencyLevel: form.urgencyLevel,
      conditionTags: form.conditionTags.length > 0 ? form.conditionTags : undefined,
      description: form.description.trim(),
      contactName: form.contactName.trim(),
      contactMobile: form.contactMobile.trim(),
      photoFileIds: photos.map((item) => item.fileId)
    };

    try {
      const detail = await submitRescueClue(payload);
      setNotice(`提交成功，线索编号：${detail.clueNo}`);
      navigate(`/me/rescue/clues/${detail.clueId}`);
    } catch (err) {
      setNotice(err.message || "提交线索失败");
    } finally {
      setSubmitting(false);
    }
  }

  return (
    <div className="page-stack">
      <section className="card page-banner">
        <p className="eyebrow">提交救助线索</p>
        <h1>提交救助线索</h1>
      </section>

      <section className="card page-form-card">
        <form className="stack-form" onSubmit={onSubmit}>
          <div className="form-grid-two">
            <label>
              城市*
              <select name="cityCode" value={form.cityCode} onChange={onCityChange} required>
                <option value="">请选择城市</option>
                {CITY_OPTIONS.map((city) => (
                  <option key={city.code} value={city.code}>
                    {city.name}
                  </option>
                ))}
              </select>
            </label>
            <label>
              区域（可选）
              <input name="districtName" value={form.districtName} onChange={onChange} maxLength={64} />
            </label>
          </div>

          <label>
            位置描述*
            <input name="locationText" value={form.locationText} onChange={onChange} maxLength={255} required />
          </label>

          <div className="form-grid-two">
            <label>
              宠物类型
              <select name="petType" value={form.petType} onChange={onChange}>
                <option value="UNKNOWN">未知</option>
                <option value="CAT">猫</option>
                <option value="DOG">狗</option>
              </select>
            </label>
            <label>
              预计数量
              <input
                name="estimatedCount"
                type="number"
                min="1"
                max="50"
                value={form.estimatedCount}
                onChange={onChange}
              />
            </label>
          </div>

          <div className="form-grid-two">
            <label>
              紧急程度*
              <select name="urgencyLevel" value={form.urgencyLevel} onChange={onChange}>
                <option value="LOW">低</option>
                <option value="MEDIUM">中</option>
                <option value="HIGH">高</option>
                <option value="EMERGENCY">紧急</option>
              </select>
            </label>
            <div>
              <p>情况标签（可多选）</p>
              <div className="tag-row">
                {CONDITION_OPTIONS.map((option) => {
                  const active = form.conditionTags.includes(option.value);
                  return (
                    <button
                      key={option.value}
                      type="button"
                      className={`soft-tag ${active ? "soft-tag-active" : ""}`}
                      onClick={() => onToggleCondition(option.value)}
                    >
                      {option.label}
                    </button>
                  );
                })}
              </div>
            </div>
          </div>

          <label>
            详细描述*
            <textarea
              name="description"
              value={form.description}
              onChange={onChange}
              maxLength={5000}
              rows={5}
              required
            />
          </label>

          <div className="form-grid-two">
            <label>
              联系人*
              <input name="contactName" value={form.contactName} onChange={onChange} maxLength={64} required />
            </label>
            <label>
              联系电话*
              <input name="contactMobile" value={form.contactMobile} onChange={onChange} maxLength={32} required />
            </label>
          </div>

          <label>
            现场图片（最多 9 张）
            <input type="file" accept="image/*" multiple onChange={onSelectPhotos} disabled={uploading || photos.length >= 9} />
          </label>

          {photos.length > 0 ? (
            <div className="upload-preview-grid">
              {photos.map((item) => (
                <div key={item.fileId} className="upload-preview-item">
                  <img src={item.url} alt={item.name || "线索图片"} />
                  <button className="secondary-btn" type="button" onClick={() => removePhoto(item.fileId)}>
                    删除
                  </button>
                </div>
              ))}
            </div>
          ) : null}

          {notice ? <p className="helper-text notice-text">{notice}</p> : null}

          <button className="primary-btn" type="submit" disabled={submitting || uploading}>
            {uploading ? "上传中..." : submitting ? "提交中..." : "提交线索"}
          </button>
        </form>
      </section>
    </div>
  );
}

export default RescueClueSubmitPage;
