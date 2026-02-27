import { Link, NavLink } from "react-router-dom";
import SectionHeader from "../components/SectionHeader.jsx";
import RoleEntryCard from "../components/RoleEntryCard.jsx";

const FEEDING_TABS = [
  { to: "/feeding/providers", label: "找服务者" },
  { to: "/feeding/orders/new", label: "发起喂养单" },
  { to: "/feeding/my-orders", label: "我的喂养单" },
  { to: "/feeding/provider-center", label: "服务者中心" },
  { to: "/feeding/my-jobs", label: "我的接单" }
];

function FeedingModulePage() {
  return (
    <div className="page-stack">
      <section className="card module-hero fade-up">
        <p className="eyebrow">喂养模块</p>
        <h1>上门喂养服务</h1>
        <p>围绕双角色路径设计：宠物主人聚焦下单与追踪，服务者聚焦接单与留痕。</p>
        <div className="hero-actions">
          <Link className="primary-btn" to="/feeding/providers">
            我是宠物主人
          </Link>
          <Link className="secondary-btn" to="/feeding/provider-center">
            我是服务者
          </Link>
        </div>
      </section>

      <section className="card page-form-card fade-up" style={{ "--delay": "60ms" }}>
        <nav className="module-tabs">
          {FEEDING_TABS.map((tab) => (
            <NavLink
              key={tab.to}
              to={tab.to}
              className={({ isActive }) => `module-tab ${isActive ? "module-tab-active" : ""}`}
            >
              {tab.label}
            </NavLink>
          ))}
        </nav>
      </section>

      <section>
        <SectionHeader
          eyebrow="角色分流"
          title="先识别角色，再进入对应流程"
          description="避免在首页平铺喂养动作，减少用户决策成本。"
        />
        <div className="role-entry-grid">
          <RoleEntryCard
            role="宠物主人"
            title="找人、发单、查看进度"
            description="适合出差、节假日、临时看护等场景。"
            entries={[
              { label: "找喂养服务者", to: "/feeding/providers" },
              { label: "发起喂养单", to: "/feeding/orders/new" },
              { label: "我的喂养单", to: "/feeding/my-orders" }
            ]}
            primaryAction={{ label: "进入主人流程", to: "/feeding/providers" }}
          />
          <RoleEntryCard
            role="服务者"
            title="完善资料、接单、沉淀留痕"
            description="通过标准流程建立服务信任和复购。"
            entries={[
              { label: "服务者中心", to: "/feeding/provider-center" },
              { label: "我的接单", to: "/feeding/my-jobs" },
              { label: "服务记录（预留）", to: "/feeding/my-jobs" }
            ]}
            primaryAction={{ label: "进入服务者流程", to: "/feeding/provider-center" }}
            delay="70ms"
          />
        </div>
      </section>
    </div>
  );
}

export default FeedingModulePage;
