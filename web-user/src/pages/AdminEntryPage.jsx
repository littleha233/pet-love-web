import { Link } from "react-router-dom";

function AdminEntryPage() {
  return (
    <div className="page-stack">
      <section className="card page-banner">
        <p className="eyebrow">页面提示</p>
        <h1>该入口为平台内部使用</h1>
        <p>如果你是普通用户，请返回首页继续使用领养与救助功能。</p>
      </section>

      <section className="card page-form-card">
        <div className="action-row">
          <Link className="primary-btn" to="/">
            返回首页
          </Link>
          <Link className="secondary-btn" to="/adoption">
            进入领养
          </Link>
          <Link className="secondary-btn" to="/rescue">
            进入救助
          </Link>
        </div>
      </section>
    </div>
  );
}

export default AdminEntryPage;
