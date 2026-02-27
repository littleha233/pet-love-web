import { useEffect, useState } from "react";
import { Link, useParams } from "react-router-dom";
import RescueClueStatusTag from "../components/RescueClueStatusTag.jsx";
import { getAdminRescueClueDetail, updateAdminRescueClueStatus } from "../api/rescueApi";
import { formatDateTime } from "../utils/format";

function AdminRescueClueDetailPage() {
  const { clueId } = useParams();
  const [detail, setDetail] = useState(null);
  const [loading, setLoading] = useState(false);
  const [notice, setNotice] = useState("");
  const [updating, setUpdating] = useState(false);
  const [form, setForm] = useState({
    status: "TRIAGED",
    triageNote: "",
    resolutionNote: "",
    suggestedResourceIdsText: ""
  });

  useEffect(() => {
    let cancelled = false;

    async function loadData() {
      setLoading(true);
      setNotice("");
      try {
        const data = await getAdminRescueClueDetail(clueId);
        if (!cancelled) {
          setDetail(data);
          setForm((prev) => ({
            ...prev,
            status: data.status === "SUBMITTED" ? "TRIAGED" : data.status || "TRIAGED",
            triageNote: data.triageNote || "",
            resolutionNote: data.resolutionNote || "",
            suggestedResourceIdsText: (data.suggestedResourceIds || []).join(",")
          }));
        }
      } catch (err) {
        if (!cancelled) {
          setNotice(err.message || "加载线索详情失败");
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
  }, [clueId]);

  function onChange(event) {
    const { name, value } = event.target;
    setForm((prev) => ({ ...prev, [name]: value }));
  }

  async function onUpdateStatus(event) {
    event.preventDefault();
    setUpdating(true);
    setNotice("");

    const suggestedResourceIds = form.suggestedResourceIdsText
      .split(",")
      .map((item) => Number(item.trim()))
      .filter((item) => Number.isFinite(item) && item > 0);

    try {
      const data = await updateAdminRescueClueStatus(clueId, {
        status: form.status,
        triageNote: form.triageNote || undefined,
        resolutionNote: form.resolutionNote || undefined,
        suggestedResourceIds
      });
      setDetail(data);
      setNotice("状态更新成功");
    } catch (err) {
      setNotice(err.message || "状态更新失败");
    } finally {
      setUpdating(false);
    }
  }

  return (
    <div className="page-stack">
      <section className="card page-banner">
        <p className="eyebrow">Admin 线索详情</p>
        <h1>{detail ? detail.clueNo : `线索 #${clueId}`}</h1>
        <p>
          <Link to="/admin/rescue/clues">返回线索列表</Link>
        </p>
      </section>

      <section className="card page-form-card">
        {loading ? <p className="helper-text">加载中...</p> : null}
        {notice ? <p className="helper-text notice-text">{notice}</p> : null}

        {detail ? (
          <div className="detail-layout-single">
            <div className="tag-row">
              <RescueClueStatusTag status={detail.status} />
              <span className="soft-tag">紧急程度：{detail.urgencyLevel}</span>
              <span className="soft-tag">宠物：{detail.petType || "UNKNOWN"}</span>
            </div>

            <p>
              提交用户：{detail.reporterUserId} ({detail.reporterNickname || "-"})
            </p>
            <p>
              城市：{detail.cityName}（{detail.cityCode}）{detail.districtName ? ` · ${detail.districtName}` : ""}
            </p>
            <p>位置：{detail.locationText}</p>
            <p>
              经纬度：{detail.geoLat ?? "-"}, {detail.geoLng ?? "-"}
            </p>
            <p>数量：{detail.estimatedCount ?? "-"}</p>
            <p>标签：{(detail.conditionTags || []).join(" / ") || "-"}</p>
            <p>描述：{detail.description}</p>
            <p>
              联系人：{detail.contactName} · 电话：{detail.contactMobile}
            </p>
            <p>分流备注：{detail.triageNote || "-"}</p>
            <p>结案备注：{detail.resolutionNote || "-"}</p>
            <p>
              处理人：{detail.handledByAdminName || "-"} · 处理时间：{formatDateTime(detail.handledAt)}
            </p>
            <p>
              创建时间：{formatDateTime(detail.createdAt)} · 更新时间：{formatDateTime(detail.updatedAt)}
            </p>

            <h3>现场图片</h3>
            <div className="detail-image-grid">
              {(detail.photos || []).map((photo) => (
                <img key={`${photo.fileId}-${photo.sortOrder}`} src={photo.url} alt="线索图片" />
              ))}
            </div>

            <form className="stack-form" onSubmit={onUpdateStatus}>
              <h3>更新状态</h3>
              <label>
                目标状态
                <select name="status" value={form.status} onChange={onChange} required>
                  <option value="TRIAGED">TRIAGED</option>
                  <option value="IN_PROGRESS">IN_PROGRESS</option>
                  <option value="RESOLVED">RESOLVED</option>
                  <option value="CLOSED">CLOSED</option>
                  <option value="INVALID">INVALID</option>
                </select>
              </label>

              <label>
                分流备注
                <input name="triageNote" value={form.triageNote} onChange={onChange} maxLength={255} />
              </label>

              <label>
                结案备注
                <input name="resolutionNote" value={form.resolutionNote} onChange={onChange} maxLength={255} />
              </label>

              <label>
                推荐资源ID（逗号分隔）
                <input
                  name="suggestedResourceIdsText"
                  value={form.suggestedResourceIdsText}
                  onChange={onChange}
                  placeholder="1,3,5"
                />
              </label>

              <button className="primary-btn" type="submit" disabled={updating}>
                {updating ? "更新中..." : "确认更新"}
              </button>
            </form>
          </div>
        ) : null}
      </section>
    </div>
  );
}

export default AdminRescueClueDetailPage;
