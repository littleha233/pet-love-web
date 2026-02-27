import { useEffect, useState } from "react";
import { Link } from "react-router-dom";
import ApplicationStatusTag from "../components/ApplicationStatusTag.jsx";
import Pagination from "../components/Pagination.jsx";
import {
  listMyAdoptionApplications,
  withdrawAdoptionApplication
} from "../api/adoptionApi";
import { formatDateTime } from "../utils/format";

const PAGE_SIZE = 10;

function MyAdoptionApplicationsPage() {
  const [status, setStatus] = useState("");
  const [page, setPage] = useState(1);
  const [result, setResult] = useState({ items: [], total: 0, page: 1, pageSize: PAGE_SIZE });
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState("");
  const [notice, setNotice] = useState("");

  async function loadData() {
    setLoading(true);
    setError("");
    try {
      const data = await listMyAdoptionApplications({
        page,
        pageSize: PAGE_SIZE,
        status: status || undefined
      });
      setResult(data);
    } catch (err) {
      setError(err.message || "加载失败");
    } finally {
      setLoading(false);
    }
  }

  useEffect(() => {
    loadData();
  }, [page, status]);

  async function onWithdraw(applicationId) {
    if (!window.confirm("确认撤回该申请？")) {
      return;
    }
    try {
      await withdrawAdoptionApplication(applicationId);
      setNotice("申请已撤回");
      await loadData();
    } catch (err) {
      setNotice(err.message || "撤回失败");
    }
  }

  return (
    <div className="page-stack">
      <section className="card page-banner">
        <p className="eyebrow">我的领养申请</p>
        <h1>查看申请进度</h1>
        <p>可按状态过滤，`SUBMITTED` 状态支持撤回。</p>
      </section>

      <section className="card page-form-card">
        <div className="inline-filter-form">
          <select
            value={status}
            onChange={(event) => {
              setStatus(event.target.value);
              setPage(1);
            }}
          >
            <option value="">全部状态</option>
            <option value="SUBMITTED">待处理</option>
            <option value="ACCEPTED">已接受</option>
            <option value="REJECTED">已拒绝</option>
            <option value="WITHDRAWN">已撤回</option>
          </select>
        </div>

        {loading ? <p className="helper-text">加载中...</p> : null}
        {error ? <p className="error-text">{error}</p> : null}
        {notice ? <p className="helper-text notice-text">{notice}</p> : null}

        <div className="list-stack">
          {result.items.map((item) => (
            <article key={item.applicationId} className="list-card">
              <div className="list-card-main">
                <h3>
                  <Link to={`/adoption/${item.postId}`}>{item.postTitle}</Link>
                </h3>
                <p className="helper-text">城市：{item.cityName}</p>
                <p className="helper-text">提交时间：{formatDateTime(item.createdAt)}</p>
                {item.handledAt ? <p className="helper-text">处理时间：{formatDateTime(item.handledAt)}</p> : null}
                <ApplicationStatusTag status={item.status} />
              </div>
              <div className="list-card-actions">
                {item.postCoverImageUrl ? <img src={item.postCoverImageUrl} alt={item.postTitle} className="tiny-cover" /> : null}
                {item.status === "SUBMITTED" ? (
                  <button className="secondary-btn" type="button" onClick={() => onWithdraw(item.applicationId)}>
                    撤回申请
                  </button>
                ) : null}
              </div>
            </article>
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

export default MyAdoptionApplicationsPage;
