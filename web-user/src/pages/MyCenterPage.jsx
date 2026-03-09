import { Link } from "react-router-dom";
import ModuleEntryCard from "../components/ModuleEntryCard.jsx";

function MyCenterPage() {
  return (
    <div className="page-stack">
      <section className="card page-banner fade-up">
        <p className="eyebrow">我的</p>
        <h1>个人中心</h1>
      </section>

      <section className="module-entry-grid">
        <ModuleEntryCard
          eyebrow="领养"
          title="我的申请与送养"
          links={[
            { label: "我的领养申请", to: "/adoption/applications" },
            { label: "我的送养", to: "/adoption/my-posts" }
          ]}
          primaryAction={{ label: "进入领养模块", to: "/adoption" }}
        />
        <ModuleEntryCard
          eyebrow="救助"
          title="我的线索与处理进展"
          links={[
            { label: "我的救助线索", to: "/me/rescue/clues" },
            { label: "提交新线索", to: "/rescue/clues/new" }
          ]}
          primaryAction={{ label: "进入救助模块", to: "/rescue" }}
          delay="70ms"
        />
      </section>

      <section className="card page-form-card fade-up" style={{ "--delay": "120ms" }}>
        <div className="action-row">
          <Link className="secondary-btn" to="/me/support/complaints">
            我的反馈记录
          </Link>
          <Link className="secondary-btn" to="/support/complaints/new">
            提交问题反馈
          </Link>
        </div>
      </section>
    </div>
  );
}

export default MyCenterPage;
