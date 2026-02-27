import { Link } from "react-router-dom";
import ModuleEntryCard from "../components/ModuleEntryCard.jsx";

function MyCenterPage() {
  return (
    <div className="page-stack">
      <section className="card page-banner fade-up">
        <p className="eyebrow">我的</p>
        <h1>个人中心</h1>
        <p>按业务线查看我的数据，减少在全局导航中反复跳转。</p>
      </section>

      <section className="module-entry-grid">
        <ModuleEntryCard
          eyebrow="领养"
          title="我的申请与送养"
          description="统一管理领养申请、发布送养与申请处理。"
          links={[
            { label: "我的领养申请", to: "/adoption/applications" },
            { label: "我的送养", to: "/adoption/my-posts" }
          ]}
          primaryAction={{ label: "进入领养模块", to: "/adoption" }}
        />
        <ModuleEntryCard
          eyebrow="喂养"
          title="订单与接单中心"
          description="查看我的喂养订单，也可切换到服务者视角处理接单。"
          links={[
            { label: "我的喂养单", to: "/feeding/my-orders" },
            { label: "我的接单", to: "/feeding/my-jobs" }
          ]}
          primaryAction={{ label: "进入喂养模块", to: "/feeding" }}
          delay="70ms"
        />
      </section>

      <section className="card page-form-card fade-up" style={{ "--delay": "120ms" }}>
        <div className="action-row">
          <Link className="secondary-btn" to="/me/rescue/clues">
            我的救助线索
          </Link>
          <Link className="secondary-btn" to="/me/support/complaints">
            我的投诉工单
          </Link>
          <Link className="secondary-btn" to="/auth/mobile-login">
            短信登录
          </Link>
        </div>
      </section>
    </div>
  );
}

export default MyCenterPage;
