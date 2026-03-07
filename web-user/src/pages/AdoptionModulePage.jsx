import { Link, NavLink } from "react-router-dom";
import SectionHeader from "../components/SectionHeader.jsx";
import ModuleEntryCard from "../components/ModuleEntryCard.jsx";

const ADOPTION_TABS = [
  { to: "/adoption/list", label: "可领养列表" },
  { to: "/adoption/post/new", label: "发布送养" },
  { to: "/adoption/applications", label: "我的申请" },
  { to: "/adoption/my-posts", label: "我的送养" }
];

function AdoptionModulePage() {
  return (
    <div className="page-stack">
      <section className="card module-hero fade-up">
        <p className="eyebrow">领养模块</p>
        <h1>领养与送养</h1>
        <p>帮助宠物找到新家，流程聚焦「浏览、申请、沟通、回访」四个关键阶段。</p>
        <div className="hero-actions">
          <Link className="primary-btn" to="/adoption/list">
            查看可领养
          </Link>
          <Link className="secondary-btn" to="/adoption/post/new">
            发布送养
          </Link>
          <Link className="secondary-btn" to="/rescue">
            进入救助模块
          </Link>
        </div>
      </section>

      <section className="card page-form-card fade-up" style={{ "--delay": "60ms" }}>
        <nav className="module-tabs">
          {ADOPTION_TABS.map((tab) => (
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
          eyebrow="流程分区"
          title="入口收口到模块内部"
          description="首页不再堆叠动作，领养相关流程统一在本模块内完成。"
        />
        <div className="module-entry-grid">
          <ModuleEntryCard
            eyebrow="我要领养"
            title="先看公开信息，再发起申请"
            description="通过城市、宠物类型筛选，找到符合家庭条件的宠物。"
            links={[
              { label: "浏览可领养列表", to: "/adoption/list" },
              { label: "查看我的申请", to: "/adoption/applications" }
            ]}
            primaryAction={{ label: "进入领养流程", to: "/adoption/list" }}
          />
          <ModuleEntryCard
            eyebrow="我要送养"
            title="发布信息并管理申请"
            description="送养信息和申请处理放在同一模块，沟通链路更集中。"
            links={[
              { label: "发布送养信息", to: "/adoption/post/new" },
              { label: "查看我的送养", to: "/adoption/my-posts" }
            ]}
            primaryAction={{ label: "进入送养流程", to: "/adoption/post/new" }}
            delay="70ms"
          />
        </div>
      </section>
    </div>
  );
}

export default AdoptionModulePage;
