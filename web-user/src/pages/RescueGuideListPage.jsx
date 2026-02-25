import { useEffect, useMemo, useState } from "react";
import { Link, useSearchParams } from "react-router-dom";
import Pagination from "../components/Pagination.jsx";
import { listRescueGuides } from "../api/rescueApi";
import { formatDateTime } from "../utils/format";

const PAGE_SIZE = 10;

function RescueGuideListPage() {
  const [searchParams] = useSearchParams();
  const initialScenarioCode = useMemo(() => searchParams.get("scenarioCode") || "", [searchParams]);

  const [draft, setDraft] = useState({
    scenarioCode: initialScenarioCode,
    cityCode: "",
    keyword: ""
  });
  const [filters, setFilters] = useState({
    scenarioCode: initialScenarioCode,
    cityCode: "",
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
        const data = await listRescueGuides({
          page,
          pageSize: PAGE_SIZE,
          scenarioCode: filters.scenarioCode || undefined,
          cityCode: filters.cityCode || undefined,
          keyword: filters.keyword || undefined
        });
        if (!cancelled) {
          setResult(data);
        }
      } catch (err) {
        if (!cancelled) {
          setError(err.message || "加载救助指引失败");
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
        <p className="eyebrow">救助指引</p>
        <h1>按场景快速找到处理步骤</h1>
        <p>仅展示已发布的指引内容，支持场景、城市和关键词筛选。</p>
      </section>

      <section className="card page-form-card">
        <form className="inline-filter-form" onSubmit={onSubmit}>
          <select name="scenarioCode" value={draft.scenarioCode} onChange={onChange}>
            <option value="">全部场景</option>
            <option value="FOUND_STRAY_CAT">发现流浪猫</option>
            <option value="FOUND_STRAY_DOG">发现流浪狗</option>
            <option value="INJURED_CAT">发现受伤猫</option>
            <option value="INJURED_DOG">发现受伤狗</option>
            <option value="ABANDONED_KITTENS">幼猫救助</option>
            <option value="ABANDONED_PUPPIES">幼犬救助</option>
            <option value="EMERGENCY_TRANSPORT">紧急转运</option>
          </select>
          <input name="cityCode" value={draft.cityCode} onChange={onChange} placeholder="城市编码（可选）" />
          <input name="keyword" value={draft.keyword} onChange={onChange} placeholder="关键词（标题/摘要/正文）" />
          <button className="primary-btn" type="submit">
            查询
          </button>
          <Link className="secondary-btn" to="/rescue/resources">
            看同城资源
          </Link>
        </form>
      </section>

      <section className="card page-form-card">
        {loading ? <p className="helper-text">加载中...</p> : null}
        {error ? <p className="error-text">{error}</p> : null}

        <div className="list-stack">
          {result.items.map((item) => (
            <article key={item.guideId} className="list-card">
              <div className="list-card-main">
                <h3>
                  #{item.guideId} {item.title}
                </h3>
                <p className="helper-text">
                  场景：{item.scenarioCode || "-"} · 城市：{item.cityCode || "全国通用"}
                </p>
                <p>{item.summary || "暂无摘要"}</p>
                <div className="tag-row">
                  {(item.tags || []).map((tag) => (
                    <span key={tag} className="soft-tag">
                      {tag}
                    </span>
                  ))}
                  <span className="soft-tag">发布时间：{formatDateTime(item.publishedAt)}</span>
                </div>
              </div>
              <div className="list-card-actions">
                <Link className="secondary-btn" to={`/rescue/guides/${item.guideId}`}>
                  查看详情
                </Link>
              </div>
            </article>
          ))}
        </div>

        {!loading && !error && result.items.length === 0 ? (
          <p className="helper-text">暂无符合条件的救助指引。</p>
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

export default RescueGuideListPage;
