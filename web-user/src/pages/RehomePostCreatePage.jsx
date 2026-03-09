import { useEffect, useRef, useState } from "react";
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

const CITY_OPTIONS = [
  { code: "310100", name: "上海" },
  { code: "330100", name: "杭州" },
  { code: "320100", name: "南京" },
  { code: "440300", name: "深圳" }
];

function RehomePostCreatePage() {
  const navigate = useNavigate();
  const [form, setForm] = useState(EMPTY_FORM);
  const [uploads, setUploads] = useState([]);
  const [uploading, setUploading] = useState(false);
  const [submitting, setSubmitting] = useState(false);
  const [message, setMessage] = useState("");
  const previewUrlsRef = useRef(new Set());

  useEffect(() => {
    return () => {
      previewUrlsRef.current.forEach((url) => URL.revokeObjectURL(url));
      previewUrlsRef.current.clear();
    };
  }, []);

  function onChange(event) {
    const { name, value } = event.target;
    setForm((prev) => ({ ...prev, [name]: value }));
  }

  function onCityChange(event) {
    const cityCode = event.target.value;
    const city = CITY_OPTIONS.find((item) => item.code === cityCode);
    setForm((prev) => ({
      ...prev,
      cityCode,
      cityName: city ? city.name : ""
    }));
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
        const previewUrl = URL.createObjectURL(file);
        previewUrlsRef.current.add(previewUrl);
        incoming.push({
          fileId: uploaded.fileId,
          url: uploaded.url || uploaded.fileUrl || "",
          previewUrl,
          fileName: uploaded.fileName || file.name
        });
      }
      setUploads((prev) => [...prev, ...incoming]);
    } catch (err) {
      setMessage(err.message || "上传失败");
    } finally {
      setUploading(false);
    }
  }

  function removeUpload(fileId) {
    setUploads((prev) => {
      const target = prev.find((item) => item.fileId === fileId);
      if (target?.previewUrl?.startsWith("blob:")) {
        URL.revokeObjectURL(target.previewUrl);
        previewUrlsRef.current.delete(target.previewUrl);
      }
      return prev.filter((item) => item.fileId !== fileId);
    });
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
        state: { notice: "发布成功，平台会尽快完成审核并展示。" }
      });
    } catch (err) {
      if (err?.code === "ADOPTION_REAL_NAME_REQUIRED" || err?.code === "VERIFICATION_REAL_NAME_REQUIRED") {
        setMessage("当前账号尚未开通领养发布权限（后端限制），请联系管理员处理。");
      } else {
        setMessage(err.message || "发布失败");
      }
    } finally {
      setSubmitting(false);
    }
  }

  return (
    <div className="page-stack">
      <section className="card page-banner">
        <p className="eyebrow">发布送养帖</p>
        <h1>填写完整信息，提升匹配效率</h1>
      </section>

      <section className="card page-form-card">
        <form className="stack-form" onSubmit={onSubmit}>
          <label>
            标题*
            <input
              name="title"
              value={form.title}
              onChange={onChange}
              maxLength={200}
              placeholder="例如：奶牛猫 1 岁，找稳定家庭（杭州）"
              required
            />
          </label>
          <label>
            正文*
            <textarea
              name="content"
              value={form.content}
              onChange={onChange}
              maxLength={5000}
              rows={8}
              placeholder={
                "可参考：\n1. 宠物性格与生活习惯\n2. 健康与疫苗情况\n3. 送养原因\n4. 希望领养人具备的条件"
              }
              required
            />
            <p className="helper-text">建议写清性格、健康、送养原因和期望条件。</p>
          </label>

          <div className="form-grid-two">
            <label>
              所在城市*
              <select name="cityCode" value={form.cityCode} onChange={onCityChange} required>
                <option value="">请选择城市</option>
                {CITY_OPTIONS.map((city) => (
                  <option key={city.code} value={city.code}>
                    {city.name}
                  </option>
                ))}
              </select>
            </label>
            <label className="full-row">
              区县 / 街道 / 小区（选填）
              <input
                name="districtName"
                value={form.districtName}
                onChange={onChange}
                placeholder="例如：余杭区良渚街道某某小区"
              />
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
                  <img src={item.previewUrl || item.url} alt={item.fileName || "上传图片"} />
                  <button type="button" className="ghost-btn" onClick={() => removeUpload(item.fileId)}>
                    删除
                  </button>
                </div>
              ))}
            </div>
          </div>

          <button className="primary-btn" type="submit" disabled={submitting || uploading}>
            {submitting ? "提交中..." : "发布送养信息"}
          </button>
          {message ? <p className="helper-text notice-text">{message}</p> : null}
        </form>
      </section>
    </div>
  );
}

export default RehomePostCreatePage;
