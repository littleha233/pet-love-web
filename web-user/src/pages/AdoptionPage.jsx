import { useEffect, useState } from "react";
import { Link } from "react-router-dom";
import SectionHeader from "../components/SectionHeader.jsx";
import Pagination from "../components/Pagination.jsx";
import AdoptionPostCard from "../components/AdoptionPostCard.jsx";
import { listAdoptionPosts } from "../api/adoptionApi";

const PAGE_SIZE = 9;
const CITY_OPTIONS = [
  { code: "", label: "全部城市" },
  { code: "310100", label: "上海" },
  { code: "330100", label: "杭州" },
  { code: "320100", label: "南京" },
  { code: "440300", label: "深圳" }
];

function AdoptionPage() {
  const [draft, setDraft] = useState({ cityCode: "", petType: "", keyword: "" });
  const [filters, setFilters] = useState({ cityCode: "", petType: "", keyword: "" });
  const [page, setPage] = useState(1);
  const [result, setResult] = useState({ items: [], total: 0, page: 1, pageSize: PAGE_SIZE });
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState("");

  useEffect(() => {
    let cancelled = false;

    async function load() {
      setLoading(true);
      setError("");
      try {
        const data = await listAdoptionPosts({
          page,
          pageSize: PAGE_SIZE,
          cityCode: filters.cityCode || undefined,
          petType: filters.petType || undefined,
          keyword: filters.keyword || undefined
        });
        if (!cancelled) {
          setResult(data);
        }
      } catch (err) {
        if (!cancelled) {
          setError(err.message || "加载列表失败");
        }
      } finally {
        if (!cancelled) {
          setLoading(false);
        }
      }
    }

    load();
    return () => {
      cancelled = true;
    };
  }, [page, filters]);

  function onFilterChange(event) {
    const { name, value } = event.target;
    setDraft((prev) => ({ ...prev, [name]: value }));
  }

  function onApplyFilters(event) {
    event.preventDefault();
    setPage(1);
    setFilters({ ...draft });
  }

  return (
    <div className="page-stack">
      <section className="card page-banner fade-up">
        <p className="eyebrow">领养列表</p>
        <h1>寻找适合的领养信息</h1>
        <p>支持按城市、宠物类型和关键词筛选，点击卡片即可查看详情并提交申请。</p>
      </section>

      <section className="card page-form-card fade-up" style={{ "--delay": "70ms" }}>
        <form className="inline-filter-form" onSubmit={onApplyFilters}>
          <select
            name="cityCode"
            value={draft.cityCode}
            onChange={onFilterChange}
          >
            {CITY_OPTIONS.map((city) => (
              <option key={city.code || "all"} value={city.code}>
                {city.label}
              </option>
            ))}
          </select>
          <select name="petType" value={draft.petType} onChange={onFilterChange}>
            <option value="">全部类型</option>
            <option value="CAT">猫</option>
            <option value="DOG">狗</option>
          </select>
          <input
            name="keyword"
            value={draft.keyword}
            onChange={onFilterChange}
            placeholder="关键词（标题/正文）"
          />
          <button className="primary-btn" type="submit">
            搜索
          </button>
          <Link className="secondary-btn" to="/adoption/post/new">
            发布送养帖
          </Link>
        </form>
      </section>

      <section>
        <SectionHeader
          eyebrow="帖子"
          title="在寻找家的孩子"
          description="以下为当前可申请的送养信息。"
        />
        {loading ? <p className="helper-text">加载中...</p> : null}
        {error ? <p className="error-text">{error}</p> : null}
        {!loading && !error && result.items.length === 0 ? (
          <p className="helper-text">当前没有符合条件的帖子。</p>
        ) : null}
        <div className="adoption-post-grid">
          {result.items.map((item) => (
            <AdoptionPostCard key={item.postId} post={item} />
          ))}
        </div>
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

export default AdoptionPage;
