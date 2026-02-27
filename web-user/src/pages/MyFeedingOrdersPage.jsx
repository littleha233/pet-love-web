import { useEffect, useState } from "react";
import { Link, useLocation, useNavigate } from "react-router-dom";
import Pagination from "../components/Pagination.jsx";
import FeedingOrderStatusTag from "../components/FeedingOrderStatusTag.jsx";
import { cancelFeedingOrder, listMyFeedingOrders } from "../api/feedingApi";
import { formatDateTime } from "../utils/format";

const PAGE_SIZE = 10;

function MyFeedingOrdersPage() {
  const navigate = useNavigate();
  const location = useLocation();
  const params = new URLSearchParams(location.search);

  const [status, setStatus] = useState(params.get("status") || "");
  const [page, setPage] = useState(1);
  const [result, setResult] = useState({ items: [], total: 0, page: 1, pageSize: PAGE_SIZE });
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState("");

  async function loadData() {
    setLoading(true);
    setError("");
    try {
      const data = await listMyFeedingOrders({
        page,
        pageSize: PAGE_SIZE,
        status: status || undefined
      });
      setResult(data);
    } catch (err) {
      setError(err.message || "加载我的订单失败");
    } finally {
      setLoading(false);
    }
  }

  useEffect(() => {
    loadData();
  }, [page, status]);

  function onApplyStatus(nextStatus) {
    setStatus(nextStatus);
    setPage(1);
    const nextParams = new URLSearchParams(location.search);
    if (nextStatus) {
      nextParams.set("status", nextStatus);
    } else {
      nextParams.delete("status");
    }
    navigate(`${location.pathname}?${nextParams.toString()}`, { replace: true });
  }

  async function onCancel(orderId) {
    try {
      await cancelFeedingOrder(orderId);
      await loadData();
    } catch (err) {
      setError(err.message || "取消订单失败");
    }
  }

  return (
    <div className="page-stack">
      <section className="card page-banner">
        <p className="eyebrow">我的订单</p>
        <h1>主人视角</h1>
        <p>查看上门喂养订单状态，待接单和未开始的已确认订单可取消。</p>
      </section>

      <section className="card page-form-card">
        <div className="action-row">
          <button className="secondary-btn" type="button" onClick={() => onApplyStatus("")}>全部</button>
          <button className="secondary-btn" type="button" onClick={() => onApplyStatus("PENDING_PROVIDER_ACCEPT")}>待接单</button>
          <button className="secondary-btn" type="button" onClick={() => onApplyStatus("CONFIRMED")}>已确认</button>
          <button className="secondary-btn" type="button" onClick={() => onApplyStatus("IN_SERVICE")}>服务中</button>
          <button className="secondary-btn" type="button" onClick={() => onApplyStatus("WAITING_OWNER_CONFIRM")}>待确认</button>
          <button className="secondary-btn" type="button" onClick={() => onApplyStatus("COMPLETED")}>已完成</button>
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
                <span>服务者：{item.providerDisplayName}</span>
                <span>城市：{item.serviceCityName}</span>
              </div>
              <p className="helper-text">上门次数：{item.visitCount}</p>
              <p className="helper-text">下次上门：{formatDateTime(item.nextVisitPlannedAt)}</p>
              <p className="helper-text">报价：{item.quotedTotalAmount ? `¥${item.quotedTotalAmount}` : "待报价"}</p>
              <p className="helper-text">更新时间：{formatDateTime(item.updatedAt)}</p>
            </div>
            <div className="list-card-actions">
              <Link className="secondary-btn" to={`/feeding/my-orders/${item.orderId}`}>
                查看详情
              </Link>
              {item.status === "PENDING_PROVIDER_ACCEPT" || item.status === "CONFIRMED" ? (
                <button className="primary-btn" type="button" onClick={() => onCancel(item.orderId)}>
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

export default MyFeedingOrdersPage;
