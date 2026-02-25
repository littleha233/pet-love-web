import { useState } from "react";
import { useNavigate } from "react-router-dom";
import { submitRescueClue, uploadRescueCluePhoto } from "../api/rescueApi";

const EMPTY_FORM = {
  cityCode: "",
  cityName: "",
  districtName: "",
  locationText: "",
  geoLat: "",
  geoLng: "",
  petType: "UNKNOWN",
  estimatedCount: "1",
  urgencyLevel: "MEDIUM",
  conditionTagsText: "",
  description: "",
  contactName: "",
  contactMobile: ""
};

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

    const conditionTags = (form.conditionTagsText || "")
      .split(",")
      .map((item) => item.trim())
      .filter(Boolean);

    const payload = {
      cityCode: form.cityCode.trim(),
      cityName: form.cityName.trim(),
      districtName: form.districtName.trim() || undefined,
      locationText: form.locationText.trim(),
      geoLat: form.geoLat.trim() ? Number(form.geoLat.trim()) : undefined,
      geoLng: form.geoLng.trim() ? Number(form.geoLng.trim()) : undefined,
      petType: form.petType || undefined,
      estimatedCount: form.estimatedCount.trim() ? Number(form.estimatedCount.trim()) : undefined,
      urgencyLevel: form.urgencyLevel,
      conditionTags: conditionTags.length > 0 ? conditionTags : undefined,
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
        <h1>记录位置与情况，便于后续分流跟进</h1>
        <p>建议上传 1~9 张现场图片。提交后可在“我的线索”查看状态变化。</p>
      </section>

      <section className="card page-form-card">
        <form className="stack-form" onSubmit={onSubmit}>
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
              区域（可选）
              <input name="districtName" value={form.districtName} onChange={onChange} maxLength={64} />
            </label>
            <label>
              位置描述*
              <input name="locationText" value={form.locationText} onChange={onChange} maxLength={255} required />
            </label>
          </div>

          <div className="form-grid-two">
            <label>
              纬度（可选）
              <input name="geoLat" value={form.geoLat} onChange={onChange} placeholder="如 31.2304" />
            </label>
            <label>
              经度（可选）
              <input name="geoLng" value={form.geoLng} onChange={onChange} placeholder="如 121.4737" />
            </label>
          </div>

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
            <label>
              情况标签（英文逗号分隔）
              <input
                name="conditionTagsText"
                value={form.conditionTagsText}
                onChange={onChange}
                placeholder="INJURED,BLEEDING,KITTEN"
              />
            </label>
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

          {notice ? <p className="helper-text">{notice}</p> : null}

          <button className="primary-btn" type="submit" disabled={submitting || uploading}>
            {uploading ? "上传中..." : submitting ? "提交中..." : "提交线索"}
          </button>
        </form>
      </section>
    </div>
  );
}

export default RescueClueSubmitPage;
