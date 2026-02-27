import { useEffect, useMemo, useState } from "react";
import { useNavigate, useSearchParams } from "react-router-dom";
import {
  createFeedingOrder,
  getFeedingProviderDetail,
  listMyFeedingPets
} from "../api/feedingApi";

const SERVICE_ITEMS = ["FEED", "WATER", "LITTER", "PLAY", "PHOTO_REPORT", "HEALTH_OBSERVATION"];

function newVisit() {
  return { plannedStartAt: "", plannedEndAt: "" };
}

function normalizeLocalDateTime(value) {
  if (!value) {
    return "";
  }
  return value.length === 16 ? `${value}:00` : value;
}

function FeedingOrderCreatePage() {
  const [searchParams] = useSearchParams();
  const navigate = useNavigate();
  const defaultProviderId = searchParams.get("providerUserId") || "";

  const [form, setForm] = useState({
    providerUserId: defaultProviderId,
    serviceCityCode: "",
    serviceCityName: "",
    serviceDistrictName: "",
    serviceAddressDetail: "",
    serviceAddressNote: "",
    contactName: "",
    contactMobile: "",
    petIdsText: "",
    selectedPetIds: [],
    serviceItemTags: ["FEED", "WATER"],
    ownerNote: "",
    requestedTotalAmount: "",
    visits: [newVisit()]
  });
  const [myPets, setMyPets] = useState([]);
  const [provider, setProvider] = useState(null);
  const [loadingPets, setLoadingPets] = useState(false);
  const [loadingProvider, setLoadingProvider] = useState(false);
  const [submitting, setSubmitting] = useState(false);
  const [message, setMessage] = useState("");

  useEffect(() => {
    let cancelled = false;

    async function loadPets() {
      setLoadingPets(true);
      try {
        const data = await listMyFeedingPets();
        if (!cancelled) {
          setMyPets(data || []);
        }
      } catch (err) {
        if (!cancelled) {
          setMessage(err.message || "加载我的宠物失败，请先设置 user token");
        }
      } finally {
        if (!cancelled) {
          setLoadingPets(false);
        }
      }
    }

    loadPets();
    return () => {
      cancelled = true;
    };
  }, []);

  useEffect(() => {
    let cancelled = false;
    const providerUserId = Number(form.providerUserId);
    if (!Number.isFinite(providerUserId) || providerUserId <= 0) {
      setProvider(null);
      return;
    }

    async function loadProvider() {
      setLoadingProvider(true);
      try {
        const data = await getFeedingProviderDetail(providerUserId);
        if (!cancelled) {
          setProvider(data);
        }
      } catch {
        if (!cancelled) {
          setProvider(null);
        }
      } finally {
        if (!cancelled) {
          setLoadingProvider(false);
        }
      }
    }

    loadProvider();
    return () => {
      cancelled = true;
    };
  }, [form.providerUserId]);

  const selectedPetIds = form.selectedPetIds;
  const canSubmit = useMemo(() => {
    const hasPets = selectedPetIds.length > 0 || form.petIdsText;
    return (
      form.providerUserId &&
      form.serviceCityCode &&
      form.serviceCityName &&
      form.serviceAddressDetail &&
      form.contactName &&
      form.contactMobile &&
      hasPets &&
      form.visits.every((item) => item.plannedStartAt && item.plannedEndAt)
    );
  }, [form, selectedPetIds]);

  function onChange(event) {
    const { name, value } = event.target;
    setForm((prev) => ({ ...prev, [name]: value }));
  }

  function onToggleTag(tag) {
    setForm((prev) => {
      const exists = prev.serviceItemTags.includes(tag);
      const next = exists
        ? prev.serviceItemTags.filter((x) => x !== tag)
        : [...prev.serviceItemTags, tag];
      return { ...prev, serviceItemTags: next };
    });
  }

  function onTogglePet(petId) {
    setForm((prev) => {
      const exists = prev.selectedPetIds.includes(petId);
      const next = exists
        ? prev.selectedPetIds.filter((id) => id !== petId)
        : [...prev.selectedPetIds, petId];
      return { ...prev, selectedPetIds: next };
    });
  }

  function onVisitChange(index, field, value) {
    setForm((prev) => {
      const visits = prev.visits.map((item, i) => (i === index ? { ...item, [field]: value } : item));
      return { ...prev, visits };
    });
  }

  function addVisit() {
    setForm((prev) => ({ ...prev, visits: [...prev.visits, newVisit()] }));
  }

  function removeVisit(index) {
    setForm((prev) => {
      if (prev.visits.length <= 1) {
        return prev;
      }
      return { ...prev, visits: prev.visits.filter((_, i) => i !== index) };
    });
  }

  async function onSubmit(event) {
    event.preventDefault();
    setSubmitting(true);
    setMessage("");

    try {
      const manualPetIds = form.petIdsText
        ? form.petIdsText
            .split(",")
            .map((item) => Number(item.trim()))
            .filter((item) => Number.isFinite(item) && item > 0)
        : [];

      const petIds = selectedPetIds.length > 0 ? selectedPetIds : manualPetIds;

      if (petIds.length === 0) {
        setMessage("请至少选择一个宠物");
        setSubmitting(false);
        return;
      }

      const payload = {
        providerUserId: Number(form.providerUserId),
        serviceCityCode: form.serviceCityCode,
        serviceCityName: form.serviceCityName,
        serviceDistrictName: form.serviceDistrictName || undefined,
        serviceAddressDetail: form.serviceAddressDetail,
        serviceAddressNote: form.serviceAddressNote || undefined,
        contactName: form.contactName,
        contactMobile: form.contactMobile,
        petIds,
        serviceItemTags: form.serviceItemTags,
        ownerNote: form.ownerNote || undefined,
        requestedTotalAmount: form.requestedTotalAmount ? Number(form.requestedTotalAmount) : undefined,
        visits: form.visits.map((item) => ({
          plannedStartAt: normalizeLocalDateTime(item.plannedStartAt),
          plannedEndAt: normalizeLocalDateTime(item.plannedEndAt)
        }))
      };

      const created = await createFeedingOrder(payload);
      navigate(`/feeding/my-orders/${created.orderId}`);
    } catch (err) {
      setMessage(err.message || "创建订单失败");
    } finally {
      setSubmitting(false);
    }
  }

  return (
    <div className="page-stack">
      <section className="card page-banner">
        <p className="eyebrow">发起预约</p>
        <h1>上门喂养订单</h1>
        <p>填写联系人、地址、宠物和上门计划，提交后进入待接单状态。</p>
      </section>

      <section className="card page-form-card">
        {loadingProvider ? <p className="helper-text">服务者信息加载中...</p> : null}
        {provider ? (
          <div className="provider-preview card page-form-card">
            <p className="helper-text">已选择服务者：{provider.displayName}</p>
            <p className="helper-text">服务城市：{provider.serviceCityName}</p>
            <p className="helper-text">参考价：{provider.basePricePerVisit ? `¥${provider.basePricePerVisit}/次` : "面议"}</p>
          </div>
        ) : null}

        <form className="stack-form" onSubmit={onSubmit}>
          <div className="form-grid-two">
            <label>
              服务者用户 ID*
              <input name="providerUserId" value={form.providerUserId} onChange={onChange} required />
            </label>
            <label>
              城市编码*
              <input name="serviceCityCode" value={form.serviceCityCode} onChange={onChange} required />
            </label>
            <label>
              城市名称*
              <input name="serviceCityName" value={form.serviceCityName} onChange={onChange} required />
            </label>
            <label>
              区县
              <input name="serviceDistrictName" value={form.serviceDistrictName} onChange={onChange} />
            </label>
            <label className="full-row">
              详细地址*
              <input name="serviceAddressDetail" value={form.serviceAddressDetail} onChange={onChange} required />
            </label>
            <label className="full-row">
              地址备注
              <input name="serviceAddressNote" value={form.serviceAddressNote} onChange={onChange} />
            </label>
            <label>
              联系人*
              <input name="contactName" value={form.contactName} onChange={onChange} required />
            </label>
            <label>
              联系电话*
              <input name="contactMobile" value={form.contactMobile} onChange={onChange} required />
            </label>
            <label>
              预算金额
              <input
                name="requestedTotalAmount"
                type="number"
                min="0"
                step="0.01"
                value={form.requestedTotalAmount}
                onChange={onChange}
              />
            </label>
          </div>

          <label>
            选择宠物（推荐）
            {loadingPets ? <p className="helper-text">加载我的宠物中...</p> : null}
            {!loadingPets && myPets.length > 0 ? (
              <div className="pet-select-grid">
                {myPets.map((pet) => {
                  const selected = selectedPetIds.includes(pet.petId);
                  return (
                    <button
                      key={pet.petId}
                      type="button"
                      className={`pet-select-item ${selected ? "pet-select-item-active" : ""}`}
                      onClick={() => onTogglePet(pet.petId)}
                    >
                      <p className="pet-select-name">{pet.petName || `宠物${pet.petId}`}</p>
                      <p className="helper-text">{pet.petType || "-"} · {pet.breed || "未知品种"}</p>
                      <p className="helper-text">ID: {pet.petId}</p>
                    </button>
                  );
                })}
              </div>
            ) : (
              <p className="helper-text">暂无可选宠物，可手动填写宠物 ID。</p>
            )}
          </label>

          <label>
            手动输入宠物 ID（逗号分隔，可选）
            <input name="petIdsText" value={form.petIdsText} onChange={onChange} placeholder="例如：1,2" />
          </label>

          <label>
            服务项
            <div className="tag-row">
              {SERVICE_ITEMS.map((tag) => (
                <button
                  key={tag}
                  type="button"
                  className={`soft-tag ${form.serviceItemTags.includes(tag) ? "soft-tag-active" : ""}`}
                  onClick={() => onToggleTag(tag)}
                >
                  {tag}
                </button>
              ))}
            </div>
          </label>

          <label>
            留言
            <textarea name="ownerNote" value={form.ownerNote} onChange={onChange} rows={4} maxLength={2000} />
          </label>

          <div className="stack-form">
            <h3>上门计划（Visit）</h3>
            {form.visits.map((visit, index) => (
              <div key={`visit-${index}`} className="form-grid-two card page-form-card">
                <label>
                  第 {index + 1} 次开始时间*
                  <input
                    type="datetime-local"
                    value={visit.plannedStartAt}
                    onChange={(e) => onVisitChange(index, "plannedStartAt", e.target.value)}
                    required
                  />
                </label>
                <label>
                  第 {index + 1} 次结束时间*
                  <input
                    type="datetime-local"
                    value={visit.plannedEndAt}
                    onChange={(e) => onVisitChange(index, "plannedEndAt", e.target.value)}
                    required
                  />
                </label>
                <button className="secondary-btn" type="button" onClick={() => removeVisit(index)}>
                  删除本次
                </button>
              </div>
            ))}
            <button className="secondary-btn" type="button" onClick={addVisit}>
              新增一次上门
            </button>
          </div>

          {message ? <p className="helper-text notice-text">{message}</p> : null}
          <button className="primary-btn" type="submit" disabled={submitting || !canSubmit}>
            {submitting ? "提交中..." : "提交预约请求"}
          </button>
        </form>
      </section>
    </div>
  );
}

export default FeedingOrderCreatePage;
