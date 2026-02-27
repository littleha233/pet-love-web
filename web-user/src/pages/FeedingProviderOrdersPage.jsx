import { useEffect, useState } from "react";
import { Link } from "react-router-dom";
import Pagination from "../components/Pagination.jsx";
import FeedingOrderStatusTag from "../components/FeedingOrderStatusTag.jsx";
import {
  cancelProviderFeedingOrder,
  listProviderFeedingOrders,
  respondProviderFeedingOrder
} from "../api/feedingApi";
import { formatDateTime } from "../utils/format";

const PAGE_SIZE = 10;

function FeedingProviderOrdersPage() {
  const [status, setStatus] = useState("");
  const [page, setPage] = useState(1);
  const [result, setResult] = useState({ items: [], total: 0, page: 1, pageSize: PAGE_SIZE });
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState("");

  async function loadData() {
    setLoading(true);
    setError("");
    try {
      const data = await listProviderFeedingOrders({
        page,
        pageSize: PAGE_SIZE,
        status: status || undefined
      });
      setResult(data);
    } catch (err) {
      setError(err.message || "加载我的接单失败");
    } finally {
      setLoading(false);
    }
  }

  useEffect(() => {
    loadData();
  }, [page, status]);

  async function onRespond(orderId, action) {
    try {
      const quotedTotalAmount =
        action === "ACCEPT"
          ? window.prompt("请输入本单报价（元），可留空", "")
          : "";
      const providerResponseNote = window.prompt("可选：给主人的说明", "") || undefined;
      await respondProviderFeedingOrder(orderId, {
        action,
        quotedTotalAmount:
          action === "ACCEPT" && quotedTotalAmount !== null && quotedTotalAmount !== ""
            ? Number(quotedTotalAmount)
            : undefined,
        providerResponseNote
      });
      await loadData();
    } catch (err) {
      setError(err.message || "操作失败");
    }
  }

  async function onCancel(orderId) {
    try {
      const reason = window.prompt("请输入取消原因", "服务者临时无法执行");
      if (!reason) {
        return;
      }
      await cancelProviderFeedingOrder(orderId, { reason });
      await loadData();
    } catch (err) {
      setError(err.message || "取消失败");
    }
  }

  return (
    <div className="page-stack">
      <section className="card page-banner">
        <p className="eyebrow">我的接单</p>
        <h1>服务者视角</h1>
        <p>处理预约请求，管理服务中和待确认订单。</p>
      </section>

      <section className="card page-form-card">
        <div className="action-row">
          <button className="secondary-btn" type="button" onClick={() => setStatus("")}>全部</button>
          <button className="secondary-btn" type="button" onClick={() => setStatus("PENDING_PROVIDER_ACCEPT")}>待接单</button>
          <button className="secondary-btn" type="button" onClick={() => setStatus("CONFIRMED")}>已确认</button>
          <button className="secondary-btn" type="button" onClick={() => setStatus("IN_SERVICE")}>服务中</button>
          <button className="secondary-btn" type="button" onClick={() => setStatus("WAITING_OWNER_CONFIRM")}>待确认</button>
          <button className="secondary-btn" type="button" onClick={() => setStatus("COMPLETED")}>已完成</button>
        </div>
      </section>

      {loading ? <p className="helper-text">加载中...</p> : null}
      {error ? <p className="error-text">{error}</p> : null}

      <section className="list-stack">
        {result.items.map((item) => (
          <article key={item.orderId} className="list-card">
            <div className="list-card-main">
              <h3>订单号：{item.orderNo}</h3>
              <div className="status-line">
                <FeedingOrderStatusTag status={item.status} />
                <span>主人：{item.ownerNickname}</span>
                <span>城市：{item.serviceCityName}</span>
              </div>
              <p className="helper-text">上门次数：{item.visitCount}</p>
              <p className="helper-text">报价：{item.quotedTotalAmount ? `¥${item.quotedTotalAmount}` : "待报价"}</p>
              <p className="helper-text">下次上门：{formatDateTime(item.nextVisitPlannedAt)}</p>
              <p className="helper-text">更新时间：{formatDateTime(item.updatedAt)}</p>
            </div>
            <div className="list-card-actions">
              <Link className="secondary-btn" to={`/feeding/my-jobs/${item.orderId}`}>
                查看详情
              </Link>
              {item.status === "PENDING_PROVIDER_ACCEPT" ? (
                <>
                  <button className="primary-btn" type="button" onClick={() => onRespond(item.orderId, "ACCEPT")}>
                    接单
                  </button>
                  <button className="secondary-btn" type="button" onClick={() => onRespond(item.orderId, "REJECT")}>
                    拒单
                  </button>
                </>
              ) : null}
              {(item.status === "PENDING_PROVIDER_ACCEPT" || item.status === "CONFIRMED") ? (
                <button className="secondary-btn" type="button" onClick={() => onCancel(item.orderId)}>
                  取消订单
                </button>
              ) : null}
            </div>
          </article>
        ))}
      </section>

      <Pagination
        page={result.page || page}
        pageSize={result.pageSize || PAGE_SIZE}
        total={result.total || 0}
        onChange={setPage}
      />
    </div>
  );
}

export default FeedingProviderOrdersPage;
