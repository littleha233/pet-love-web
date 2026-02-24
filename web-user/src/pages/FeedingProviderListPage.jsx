import { useEffect, useState } from "react";
import SectionHeader from "../components/SectionHeader.jsx";
import Pagination from "../components/Pagination.jsx";
import FeedingProviderCard from "../components/FeedingProviderCard.jsx";
import { listFeedingProviders } from "../api/feedingApi";

const PAGE_SIZE = 9;

function FeedingProviderListPage() {
  const [draft, setDraft] = useState({ cityCode: "", petType: "", keyword: "", sortBy: "DEFAULT" });
  const [filters, setFilters] = useState({ cityCode: "", petType: "", keyword: "", sortBy: "DEFAULT" });
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
        const data = await listFeedingProviders({
          page,
          pageSize: PAGE_SIZE,
          cityCode: filters.cityCode || undefined,
          petType: filters.petType || undefined,
          keyword: filters.keyword || undefined,
          sortBy: filters.sortBy || undefined
        });
        if (!cancelled) {
          setResult(data);
        }
      } catch (err) {
        if (!cancelled) {
          setError(err.message || "加载服务者列表失败");
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
      <section className="card page-banner fade-up">
        <p className="eyebrow">上门喂养</p>
        <h1>服务者列表</h1>
        <p>按城市、宠物类型和关键词筛选，查看服务者资料并发起预约请求。</p>
      </section>

      <section className="card page-form-card fade-up" style={{ "--delay": "70ms" }}>
        <form className="inline-filter-form" onSubmit={onSubmit}>
          <input name="cityCode" value={draft.cityCode} onChange={onChange} placeholder="城市编码（如 310100）" />
          <select name="petType" value={draft.petType} onChange={onChange}>
            <option value="">全部宠物类型</option>
            <option value="CAT">猫</option>
            <option value="DOG">狗</option>
          </select>
          <input name="keyword" value={draft.keyword} onChange={onChange} placeholder="关键词（昵称/简介）" />
          <select name="sortBy" value={draft.sortBy} onChange={onChange}>
            <option value="DEFAULT">默认排序</option>
            <option value="RATING">评分优先</option>
            <option value="LATEST">最近更新</option>
          </select>
          <button className="primary-btn" type="submit">
            搜索
          </button>
        </form>
      </section>

      <section>
        <SectionHeader eyebrow="服务者" title="可预约服务者" description="仅展示 ACTIVE 状态资料。" />
        {loading ? <p className="helper-text">加载中...</p> : null}
        {error ? <p className="error-text">{error}</p> : null}
        {!loading && !error && result.items.length === 0 ? <p className="helper-text">暂无符合条件的服务者。</p> : null}

        <div className="adoption-post-grid">
          {result.items.map((provider) => (
            <FeedingProviderCard key={provider.providerProfileId} provider={provider} />
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

export default FeedingProviderListPage;
