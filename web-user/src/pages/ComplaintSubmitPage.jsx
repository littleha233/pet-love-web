import { useState } from "react";
import { useNavigate } from "react-router-dom";
import { submitComplaintTicket, uploadComplaintEvidence } from "../api/opsApi";

const EMPTY_FORM = {
  targetType: "OTHER",
  targetId: "",
  title: "",
  content: "",
  priority: "MEDIUM",
  contactMobile: ""
};

function ComplaintSubmitPage() {
  const navigate = useNavigate();
  const [form, setForm] = useState(EMPTY_FORM);
  const [evidencePhotos, setEvidencePhotos] = useState([]);
  const [notice, setNotice] = useState("");
  const [uploading, setUploading] = useState(false);
  const [submitting, setSubmitting] = useState(false);

  function onChange(event) {
    const { name, value } = event.target;
    setForm((prev) => ({ ...prev, [name]: value }));
  }

  async function onSelectPhotos(event) {
    const files = Array.from(event.target.files || []);
    if (files.length === 0) {
      return;
    }

    const remain = 9 - evidencePhotos.length;
    if (remain <= 0) {
      setNotice("最多上传 9 张证据图片");
      return;
    }

    setUploading(true);
    setNotice("");
    try {
      const uploadTargets = files.slice(0, remain);
      const uploaded = [];
      for (const file of uploadTargets) {
        const result = await uploadComplaintEvidence(file);
        uploaded.push({ fileId: result.fileId, url: result.url, name: file.name });
      }
      setEvidencePhotos((prev) => [...prev, ...uploaded]);
      if (files.length > remain) {
        setNotice(`仅上传前 ${remain} 张，已达到上限 9 张`);
      }
    } catch (err) {
      setNotice(err.message || "证据图片上传失败");
    } finally {
      setUploading(false);
      event.target.value = "";
    }
  }

  function removePhoto(fileId) {
    setEvidencePhotos((prev) => prev.filter((item) => item.fileId !== fileId));
  }

  async function onSubmit(event) {
    event.preventDefault();
    setSubmitting(true);
    setNotice("");

    const payload = {
      targetType: form.targetType,
      targetId: form.targetId.trim() ? Number(form.targetId.trim()) : undefined,
      title: form.title.trim(),
      content: form.content.trim(),
      priority: form.priority,
      contactMobile: form.contactMobile.trim() || undefined,
      evidenceFileIds: evidencePhotos.map((item) => item.fileId)
    };

    try {
      const detail = await submitComplaintTicket(payload);
      setNotice(`提交成功，工单号：${detail.ticketNo}`);
      navigate(`/me/support/complaints/${detail.ticketId}`);
    } catch (err) {
      setNotice(err.message || "提交投诉失败");
    } finally {
      setSubmitting(false);
    }
  }

  return (
    <div className="page-stack">
      <section className="card page-banner">
        <p className="eyebrow">帮助与投诉</p>
        <h1>提交投诉工单</h1>
        <p>可填写对象类型与编号，上传证据图片后提交。平台会在工单里回复处理进展。</p>
      </section>

      <section className="card page-form-card">
        <form className="stack-form" onSubmit={onSubmit}>
          <div className="form-grid-two">
            <label>
              投诉对象类型*
              <select name="targetType" value={form.targetType} onChange={onChange} required>
                <option value="ADOPTION_POST">送养帖子</option>
                <option value="ADOPTION_APPLICATION">领养申请</option>
                <option value="FEEDING_ORDER">喂养订单</option>
                <option value="FEEDING_PROVIDER">喂养服务者</option>
                <option value="RESCUE_RESOURCE">救助资源</option>
                <option value="USER">用户</option>
                <option value="OTHER">其他</option>
              </select>
            </label>
            <label>
              投诉对象 ID（可选）
              <input name="targetId" value={form.targetId} onChange={onChange} placeholder="如 10001" />
            </label>
          </div>

          <label>
            标题*
            <input name="title" value={form.title} onChange={onChange} maxLength={200} required />
          </label>

          <label>
            详细描述*
            <textarea
              name="content"
              value={form.content}
              onChange={onChange}
              rows={6}
              maxLength={5000}
              required
            />
          </label>

          <div className="form-grid-two">
            <label>
              优先级
              <select name="priority" value={form.priority} onChange={onChange}>
                <option value="LOW">低</option>
                <option value="MEDIUM">中</option>
                <option value="HIGH">高</option>
                <option value="URGENT">紧急</option>
              </select>
            </label>
            <label>
              联系手机号（可选）
              <input name="contactMobile" value={form.contactMobile} onChange={onChange} maxLength={32} />
            </label>
          </div>

          <label>
            证据图片（最多 9 张）
            <input
              type="file"
              accept="image/*"
              multiple
              onChange={onSelectPhotos}
              disabled={uploading || evidencePhotos.length >= 9}
            />
          </label>

          {evidencePhotos.length > 0 ? (
            <div className="upload-preview-grid">
              {evidencePhotos.map((item) => (
                <div key={item.fileId} className="upload-preview-item">
                  <img src={item.url} alt={item.name || "证据图片"} />
                  <button className="secondary-btn" type="button" onClick={() => removePhoto(item.fileId)}>
                    删除
                  </button>
                </div>
              ))}
            </div>
          ) : null}

          {notice ? <p className="helper-text notice-text">{notice}</p> : null}

          <button className="primary-btn" type="submit" disabled={submitting || uploading}>
            {uploading ? "上传中..." : submitting ? "提交中..." : "提交投诉"}
          </button>
        </form>
      </section>
    </div>
  );
}

export default ComplaintSubmitPage;
