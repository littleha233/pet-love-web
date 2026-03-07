import { useEffect, useMemo, useState } from "react";
import { Link, useSearchParams } from "react-router-dom";
import Pagination from "../components/Pagination.jsx";
import { listRescueResources } from "../api/rescueApi";
import { formatDateTime, formatRescueResourceType } from "../utils/format";

const PAGE_SIZE = 10;
const CITY_OPTIONS = [
  { code: "", name: "全部城市" },
  { code: "310100", name: "上海" },
  { code: "330100", name: "杭州" },
  { code: "320100", name: "南京" },
  { code: "440300", name: "深圳" }
];

function RescueResourceListPage() {
  const [searchParams] = useSearchParams();
  const initialCityCode = useMemo(() => searchParams.get("cityCode") || "", [searchParams]);

  const [draft, setDraft] = useState({
    cityCode: initialCityCode,
    resourceType: "",
    petType: "",
    keyword: ""
  });
  const [filters, setFilters] = useState({
    cityCode: initialCityCode,
    resourceType: "",
    petType: "",
    keyword: ""
  });
  const [page, setPage] = useState(1);
  const [result, setResult] = useState({ items: [], total: 0, page: 1, pageSize: PAGE_SIZE });
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState("");

  useEffect(() => {
    let cancelled = false;

    async function loadData() {
      setLoading(true);
      setError("");
      try {
        const data = await listRescueResources({
          page,
          pageSize: PAGE_SIZE,
          cityCode: filters.cityCode || undefined,
          resourceType: filters.resourceType || undefined,
          petType: filters.petType || undefined,
          keyword: filters.keyword || undefined
        });
        if (!cancelled) {
          setResult(data);
        }
      } catch (err) {
        if (!cancelled) {
          setError(err.message || "加载资源目录失败");
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
  }, [page, filters]);

  function onChange(event) {
    const { name, value } = event.target;
    setDraft((prev) => ({ ...prev, [name]: value }));
  }

  function onSubmit(event) {
    event.preventDefault();
    setPage(1);
    setFilters({ ...draft });
  }

  return (
    <div className="page-stack">
      <section className="card page-banner">
        <p className="eyebrow">救助资源目录</p>
        <h1>同城医院、救助站、志愿者与官方渠道</h1>
        <p>仅展示启用中的资源，建议优先选择最近核验时间较新的联系方式。</p>
      </section>

      <section className="card page-form-card">
        <form className="inline-filter-form" onSubmit={onSubmit}>
          <select name="cityCode" value={draft.cityCode} onChange={onChange}>
            {CITY_OPTIONS.map((city) => (
              <option key={city.code || "all"} value={city.code}>
                {city.name}
              </option>
            ))}
          </select>
          <select name="resourceType" value={draft.resourceType} onChange={onChange}>
            <option value="">全部资源类型</option>
            <option value="ANIMAL_HOSPITAL">动物医院</option>
            <option value="SHELTER">救助站</option>
            <option value="VOLUNTEER_GROUP">志愿者组织</option>
            <option value="OFFICIAL_CHANNEL">官方渠道</option>
            <option value="NGO">公益组织</option>
          </select>
          <select name="petType" value={draft.petType} onChange={onChange}>
            <option value="">全部宠物类型</option>
            <option value="CAT">猫</option>
            <option value="DOG">狗</option>
          </select>
          <input name="keyword" value={draft.keyword} onChange={onChange} placeholder="关键词（名称/服务范围）" />
          <button className="primary-btn" type="submit">
            查询
          </button>
        </form>
      </section>

      <section className="card page-form-card">
        {loading ? <p className="helper-text">加载中...</p> : null}
        {error ? <p className="error-text">{error}</p> : null}

        <div className="list-stack">
          {result.items.map((item) => (
            <article key={item.resourceId} className="list-card">
              <div className="list-card-main">
                <h3>{item.name}</h3>
                <p className="helper-text">
                  类型：{formatRescueResourceType(item.resourceType)} · 城市：{item.cityName || "未标注"}
                  {item.districtName ? ` · ${item.districtName}` : ""}
                </p>
                <p>{item.serviceScope || "暂无服务范围说明"}</p>
                <div className="tag-row">
                  {(item.capabilityTags || []).map((tag) => (
                    <span key={tag} className="soft-tag">
                      {tag}
                    </span>
                  ))}
                  <span className="soft-tag">电话：{item.contactPhoneMasked || "-"}</span>
                  <span className="soft-tag">核验：{formatDateTime(item.verifiedAt)}</span>
                </div>
              </div>
              <div className="list-card-actions">
                <Link className="secondary-btn" to={`/rescue/resources/${item.resourceId}`}>
                  查看详情
                </Link>
              </div>
            </article>
          ))}
        </div>

        {!loading && !error && result.items.length === 0 ? (
          <p className="helper-text">暂无符合条件的救助资源。</p>
        ) : null}

        <Pagination
          page={result.page || page}
          pageSize={result.pageSize || PAGE_SIZE}
          total={result.total || 0}
          onChange={setPage}
        />
      </section>
    </div>
  );
}

export default RescueResourceListPage;
