import { useState } from "react";
import { submitProviderVisitLog, uploadFeedingLogImage } from "../api/feedingApi";

function FeedingVisitEditor({ visit, onSuccess }) {
  const [form, setForm] = useState({
    foodDone: false,
    waterDone: false,
    litterDone: false,
    playDone: false,
    healthObservation: "",
    visitNote: "",
    photoFileIds: [],
    photoUrls: []
  });
  const [uploading, setUploading] = useState(false);
  const [submitting, setSubmitting] = useState(false);
  const [message, setMessage] = useState("");

  function onBooleanChange(event) {
    const { name, checked } = event.target;
    setForm((prev) => ({ ...prev, [name]: checked }));
  }

  function onTextChange(event) {
    const { name, value } = event.target;
    setForm((prev) => ({ ...prev, [name]: value }));
  }

  async function onUpload(event) {
    const file = event.target.files?.[0];
    if (!file) {
      return;
    }
    setUploading(true);
    setMessage("");
    try {
      const uploaded = await uploadFeedingLogImage(file);
      setForm((prev) => ({
        ...prev,
        photoFileIds: [...prev.photoFileIds, uploaded.fileId].slice(0, 9),
        photoUrls: [...prev.photoUrls, uploaded.url].slice(0, 9)
      }));
      setMessage("图片上传成功");
    } catch (err) {
      setMessage(err.message || "图片上传失败");
    } finally {
      setUploading(false);
      event.target.value = "";
    }
  }

  async function onSubmit(event) {
    event.preventDefault();
    setSubmitting(true);
    setMessage("");
    try {
      await submitProviderVisitLog(visit.visitId, {
        foodDone: form.foodDone,
        waterDone: form.waterDone,
        litterDone: form.litterDone,
        playDone: form.playDone,
        healthObservation: form.healthObservation || undefined,
        visitNote: form.visitNote || undefined,
        photoFileIds: form.photoFileIds
      });
      setMessage("留痕提交成功");
      onSuccess();
    } catch (err) {
      setMessage(err.message || "提交失败");
    } finally {
      setSubmitting(false);
    }
  }

  return (
    <form className="stack-form card visit-editor" onSubmit={onSubmit}>
      <h4>提交本次留痕</h4>
      <label className="inline-check">
        <input type="checkbox" name="foodDone" checked={form.foodDone} onChange={onBooleanChange} />
        已喂食
      </label>
      <label className="inline-check">
        <input type="checkbox" name="waterDone" checked={form.waterDone} onChange={onBooleanChange} />
        已换水
      </label>
      <label className="inline-check">
        <input type="checkbox" name="litterDone" checked={form.litterDone} onChange={onBooleanChange} />
        已清理猫砂/环境
      </label>
      <label className="inline-check">
        <input type="checkbox" name="playDone" checked={form.playDone} onChange={onBooleanChange} />
        已互动陪玩
      </label>
      <label>
        健康观察
        <input
          name="healthObservation"
          value={form.healthObservation}
          onChange={onTextChange}
          maxLength={255}
        />
      </label>
      <label>
        备注
        <textarea name="visitNote" value={form.visitNote} onChange={onTextChange} maxLength={2000} rows={4} />
      </label>
      <label>
        上传照片（最多 9 张）
        <input type="file" accept="image/*" onChange={onUpload} disabled={uploading || form.photoFileIds.length >= 9} />
      </label>
      <div className="detail-image-grid">
        {form.photoUrls.map((url, index) => (
          <img key={`${url}-${index}`} src={url} alt="留痕" />
        ))}
      </div>
      {message ? <p className="helper-text notice-text">{message}</p> : null}
      <button className="primary-btn" type="submit" disabled={submitting || uploading}>
        {submitting ? "提交中..." : "提交留痕"}
      </button>
    </form>
  );
}

export default FeedingVisitEditor;
