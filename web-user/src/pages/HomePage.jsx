import { Link } from "react-router-dom";
import Hero from "../components/Hero.jsx";
import SectionHeader from "../components/SectionHeader.jsx";
import ModuleEntryCard from "../components/ModuleEntryCard.jsx";
import TrustBadge from "../components/TrustBadge.jsx";
import FeaturePreviewList from "../components/FeaturePreviewList.jsx";
import {
  adoptionFlowSteps,
  adoptionPreviewItems,
  homepageTrustBadges,
  rescueQuickLinks
} from "../data/mockData.js";

const rescueFlowSteps = [
  "先确认人身与现场安全",
  "按场景指引做基础处置",
  "联系同城救助资源并记录进展",
  "提交线索，便于平台后续跟进"
];

const supportPreviewItems = [
  {
    title: "问题反馈入口",
    subtitle: "针对领养与救助相关问题提交反馈并跟踪处理。",
    to: "/support/complaints/new"
  },
  {
    title: "我的反馈记录",
    subtitle: "查看回复、补充证据、确认处理结果。",
    to: "/me/support/complaints"
  },
  {
    title: "我的救助线索",
    subtitle: "查看线索状态、推荐资源与处理备注。",
    to: "/me/rescue/clues"
  }
];

function HomePage() {
  return (
    <div className="page-stack">
      <Hero
        title="聚焦领养与救助，让每次行动都有结果"
        subtitle="平台当前阶段集中做两件事：帮助宠物找到新家，以及在突发场景下提供可执行的救助闭环。"
        imageUrl="https://images.pexels.com/photos/4587994/pexels-photo-4587994.jpeg?auto=compress&cs=tinysrgb&w=1200"
        imageAlt="主人与宠物互动"
        primaryText="进入领养"
        primaryTo="/adoption"
        secondaryText="进入救助"
        secondaryTo="/rescue"
        highlights={homepageTrustBadges}
      />

      <section>
        <SectionHeader
          eyebrow="业务分发"
          title="先选业务线，再进入对应流程"
          description="首页只做分发与信任背书，流程动作统一收口到领养与救助模块。"
        />
        <div className="module-entry-grid">
          <ModuleEntryCard
            eyebrow="领养中心"
            title="查看可领养，或发布送养"
            description="面向领养人与送养人，统一管理申请、发布和跟进。"
            links={[
              { label: "查看可领养", to: "/adoption/list" },
              { label: "发布送养", to: "/adoption/post/new" },
              { label: "我的申请", to: "/adoption/applications" },
              { label: "我的送养", to: "/adoption/my-posts" }
            ]}
            primaryAction={{ label: "进入领养", to: "/adoption" }}
          />
          <ModuleEntryCard
            eyebrow="救助中心"
            title="先看指引，再找资源，必要时提线索"
            description="从现场处置到后续跟进，页面链路完整可追踪。"
            links={[
              { label: "查看救助指引", to: "/rescue/guides" },
              { label: "查看救助资源", to: "/rescue/resources" },
              { label: "提交救助线索", to: "/rescue/clues/new" },
              { label: "我的救助线索", to: "/me/rescue/clues" }
            ]}
            primaryAction={{ label: "进入救助", to: "/rescue" }}
            delay="70ms"
          />
        </div>
      </section>

      <section>
        <SectionHeader
          eyebrow="流程说明"
          title="两条流程各自清晰"
          description="遇到实际场景时先按流程走，减少临场决策成本。"
        />
        <div className="flow-explain-grid">
          <article className="card flow-explain-card fade-up">
            <h3>领养流程</h3>
            <ol>
              {adoptionFlowSteps.map((step) => (
                <li key={step}>{step}</li>
              ))}
            </ol>
          </article>
          <article className="card flow-explain-card fade-up" style={{ "--delay": "70ms" }}>
            <h3>救助流程</h3>
            <ol>
              {rescueFlowSteps.map((step) => (
                <li key={step}>{step}</li>
              ))}
            </ol>
          </article>
        </div>
      </section>

      <section>
        <SectionHeader
          eyebrow="内容预览"
          title="首页只预览，不在首页完成流程"
          description="点击后进入对应模块继续操作。"
        />
        <div className="feature-preview-grid">
          <FeaturePreviewList
            title="最新领养信息"
            description="优先展示近期更新且信息完整的内容。"
            items={adoptionPreviewItems}
            action={{ label: "查看全部领养信息", to: "/adoption/list" }}
          />
          <FeaturePreviewList
            title="救助快捷入口"
            description="按场景进入对应指引，降低误操作风险。"
            items={rescueQuickLinks}
            action={{ label: "进入救助模块", to: "/rescue" }}
          />
          <FeaturePreviewList
            title="保障与反馈"
            description="问题可追踪、证据可补充、进展可查看。"
            items={supportPreviewItems}
            action={{ label: "提交问题反馈", to: "/support/complaints/new" }}
          />
        </div>
      </section>

      <section className="card trust-footer-panel fade-up">
        <div className="trust-footer-header">
          <p className="eyebrow">平台保障</p>
          <h3>温暖治愈，也要专业可信</h3>
          <p>领养审核、线索处理与问题反馈都应有明确闭环。</p>
        </div>
        <div className="trust-badge-row">
          {homepageTrustBadges.map((item) => (
            <TrustBadge key={item} text={item} />
          ))}
        </div>
        <div className="trust-footer-actions">
          <Link className="primary-btn" to="/support/complaints/new">
            问题反馈入口
          </Link>
          <Link className="secondary-btn" to="/rescue/resources">
            查看救助资源
          </Link>
        </div>
      </section>
    </div>
  );
}

export default HomePage;
