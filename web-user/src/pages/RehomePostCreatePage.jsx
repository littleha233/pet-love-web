import { useState } from "react";
import { useNavigate } from "react-router-dom";
import PetInfoForm from "../components/PetInfoForm.jsx";
import { createRehomePost, uploadPetImage } from "../api/adoptionApi";

const EMPTY_FORM = {
  title: "",
  content: "",
  cityCode: "",
  cityName: "",
  districtName: "",
  petType: "CAT",
  petName: "",
  petGender: "",
  ageMonths: "",
  breed: "",
  weightKg: "",
  neuteredStatus: "",
  vaccinatedStatus: "",
  healthNote: "",
  temperamentTags: "",
  specialCareNote: ""
};

function RehomePostCreatePage() {
  const navigate = useNavigate();
  const [form, setForm] = useState(EMPTY_FORM);
  const [uploads, setUploads] = useState([]);
  const [uploading, setUploading] = useState(false);
  const [submitting, setSubmitting] = useState(false);
  const [message, setMessage] = useState("");

  function onChange(event) {
    const { name, value } = event.target;
    setForm((prev) => ({ ...prev, [name]: value }));
  }

  async function onUpload(event) {
    const files = Array.from(event.target.files || []);
    event.target.value = "";

    if (files.length === 0) {
      return;
    }
    if (uploads.length + files.length > 9) {
      setMessage("最多上传 9 张图片");
      return;
    }

    setUploading(true);
    setMessage("");
    const incoming = [];

    try {
      for (const file of files) {
        const uploaded = await uploadPetImage(file);
        incoming.push({ fileId: uploaded.fileId, url: uploaded.url, fileName: uploaded.fileName });
      }
      setUploads((prev) => [...prev, ...incoming]);
    } catch (err) {
      setMessage(err.message || "上传失败");
    } finally {
      setUploading(false);
    }
  }

  function removeUpload(fileId) {
    setUploads((prev) => prev.filter((item) => item.fileId !== fileId));
  }

  async function onSubmit(event) {
    event.preventDefault();

    if (uploads.length < 1) {
      setMessage("请至少上传 1 张宠物图片");
      return;
    }

    const tags = form.temperamentTags
      .split(",")
      .map((item) => item.trim())
      .filter(Boolean)
      .slice(0, 10);

    const payload = {
      title: form.title,
      content: form.content,
      cityCode: form.cityCode,
      cityName: form.cityName,
      districtName: form.districtName || undefined,
      petType: form.petType,
      petName: form.petName || undefined,
      petGender: form.petGender || undefined,
      ageMonths: form.ageMonths === "" ? undefined : Number(form.ageMonths),
      breed: form.breed || undefined,
      weightKg: form.weightKg === "" ? undefined : Number(form.weightKg),
      neuteredStatus: form.neuteredStatus || undefined,
      vaccinatedStatus: form.vaccinatedStatus || undefined,
      healthNote: form.healthNote || undefined,
      temperamentTags: tags.length > 0 ? tags : undefined,
      specialCareNote: form.specialCareNote || undefined,
      petImageFileIds: uploads.map((item) => item.fileId)
    };

    setSubmitting(true);
    setMessage("");

    try {
      await createRehomePost(payload);
      navigate("/adoption/my-posts", {
        state: { notice: "发布成功，帖子状态为待审核。" }
      });
    } catch (err) {
      setMessage(err.message || "发布失败");
    } finally {
      setSubmitting(false);
    }
  }

  return (
    <div className="page-stack">
      <section className="card page-banner">
        <p className="eyebrow">发布送养帖</p>
        <h1>发布后将进入审核队列</h1>
        <p>需要用户登录且实名认证通过，图片文件会校验归属和 READY 状态。</p>
      </section>

      <section className="card page-form-card">
        <form className="stack-form" onSubmit={onSubmit}>
          <label>
            标题*
            <input name="title" value={form.title} onChange={onChange} maxLength={200} required />
          </label>
          <label>
            正文*
            <textarea name="content" value={form.content} onChange={onChange} maxLength={5000} rows={6} required />
          </label>

          <div className="form-grid-two">
            <label>
              城市编码*
              <input name="cityCode" value={form.cityCode} onChange={onChange} required />
            </label>
            <label>
              城市名称*
              <input name="cityName" value={form.cityName} onChange={onChange} required />
            </label>
            <label className="full-row">
              区县
              <input name="districtName" value={form.districtName} onChange={onChange} />
            </label>
          </div>

          <h3>宠物信息</h3>
          <PetInfoForm form={form} onChange={onChange} />

          <div className="stack-form">
            <label>
              上传宠物图片（1~9）
              <input type="file" accept="image/*" multiple onChange={onUpload} disabled={uploading} />
            </label>
            <p className="helper-text">{uploading ? "上传中..." : `已上传 ${uploads.length} 张`}</p>
            <div className="upload-preview-grid">
              {uploads.map((item) => (
                <div key={item.fileId} className="upload-preview-item">
                  <img src={item.url} alt={item.fileName || "上传图片"} />
                  <button type="button" className="ghost-btn" onClick={() => removeUpload(item.fileId)}>
                    删除
                  </button>
                </div>
              ))}
            </div>
          </div>

          <button className="primary-btn" type="submit" disabled={submitting || uploading}>
            {submitting ? "提交中..." : "发布并提交审核"}
          </button>
          {message ? <p className="helper-text notice-text">{message}</p> : null}
        </form>
      </section>
    </div>
  );
}

export default RehomePostCreatePage;
