import { useEffect, useState } from "react";
import { Link, useParams } from "react-router-dom";
import ApplicationStatusTag from "../components/ApplicationStatusTag.jsx";
import Pagination from "../components/Pagination.jsx";
import {
  getAdoptionPostDetail,
  handleAdoptionApplication,
  listPostApplications
} from "../api/adoptionApi";
import { formatDateTime } from "../utils/format";

const PAGE_SIZE = 10;

function MyRehomePostApplicationsPage() {
  const { postId } = useParams();
  const [page, setPage] = useState(1);
  const [post, setPost] = useState(null);
  const [result, setResult] = useState({ items: [], total: 0, page: 1, pageSize: PAGE_SIZE });
  const [loading, setLoading] = useState(false);
  const [message, setMessage] = useState("");
  const [error, setError] = useState("");

  async function loadData() {
    setLoading(true);
    setError("");
    try {
      const [postDetail, applications] = await Promise.all([
        getAdoptionPostDetail(postId),
        listPostApplications(postId, { page, pageSize: PAGE_SIZE })
      ]);
      setPost(postDetail);
      setResult(applications);
    } catch (err) {
      setError(err.message || "加载失败");
    } finally {
      setLoading(false);
    }
  }

  useEffect(() => {
    loadData();
  }, [postId, page]);

  async function onHandle(applicationId, action) {
    const note = window.prompt(
      action === "ACCEPT" ? "可选：填写接受备注" : "可选：填写拒绝备注",
      ""
    );

    try {
      await handleAdoptionApplication(applicationId, {
        action,
        decisionNote: note || undefined
      });
      setMessage(action === "ACCEPT" ? "已接受申请，帖子会自动关闭。" : "已拒绝申请。\n");
      await loadData();
    } catch (err) {
      setMessage(err.message || "处理失败");
    }
  }

  return (
    <div className="page-stack">
      <section className="card page-banner">
        <p className="eyebrow">收到的申请</p>
        <h1>{post ? post.title : `帖子 #${postId}`}</h1>
        <p>
          <Link to="/adoption/my-posts">返回我的发布</Link>
        </p>
      </section>

      <section className="card page-form-card">
        {loading ? <p className="helper-text">加载中...</p> : null}
        {error ? <p className="error-text">{error}</p> : null}
        {message ? <p className="helper-text notice-text">{message}</p> : null}

        <div className="list-stack">
          {result.items.map((item) => (
            <article key={item.applicationId} className="list-card">
              <div className="list-card-main">
                <p>
                  申请人：{item.applicant?.nickname || `用户${item.applicant?.userId || "-"}`}
                  {item.applicant?.isRealNameVerified ? "（已实名）" : ""}
                </p>
                <p className="helper-text">提交时间：{formatDateTime(item.createdAt)}</p>
                <ApplicationStatusTag status={item.status} />
                <p>留言：{item.message}</p>
                {item.livingEnvNote ? <p>居住环境：{item.livingEnvNote}</p> : null}
                {item.petExperienceNote ? <p>养宠经验：{item.petExperienceNote}</p> : null}
                {item.decisionNote ? <p className="helper-text">处理备注：{item.decisionNote}</p> : null}
                {item.handledAt ? <p className="helper-text">处理时间：{formatDateTime(item.handledAt)}</p> : null}
              </div>
              {item.status === "SUBMITTED" ? (
                <div className="list-card-actions">
                  <button className="primary-btn" type="button" onClick={() => onHandle(item.applicationId, "ACCEPT")}>
                    接受
                  </button>
                  <button className="secondary-btn" type="button" onClick={() => onHandle(item.applicationId, "REJECT")}>
                    拒绝
                  </button>
                </div>
              ) : null}
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

export default MyRehomePostApplicationsPage;
