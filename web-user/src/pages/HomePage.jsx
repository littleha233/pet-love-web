import { Link } from "react-router-dom";
import Hero from "../components/Hero.jsx";
import SectionHeader from "../components/SectionHeader.jsx";
import ModuleEntryCard from "../components/ModuleEntryCard.jsx";
import TrustBadge from "../components/TrustBadge.jsx";
import FeaturePreviewList from "../components/FeaturePreviewList.jsx";
import {
  adoptionFlowSteps,
  adoptionPreviewItems,
  feedingFlowSteps,
  feedingProviderPreviewItems,
  homepageTrustBadges,
  rescueQuickLinks
} from "../data/mockData.js";

function HomePage() {
  return (
    <div className="page-stack">
      <Hero
        title="帮助它找到家，也让托付更安心"
        subtitle="一个以领养、上门喂养、救助指引为核心的宠物服务平台。我们坚持温暖表达，也坚持流程透明与可追溯。"
        imageUrl="https://images.pexels.com/photos/4587994/pexels-photo-4587994.jpeg?auto=compress&cs=tinysrgb&w=1200"
        imageAlt="主人与宠物互动"
        primaryText="进入领养"
        primaryTo="/adoption"
        secondaryText="进入喂养"
        secondaryTo="/feeding"
        highlights={homepageTrustBadges}
      />

      <section>
        <SectionHeader
          eyebrow="业务分发"
          title="先选业务线，再进入对应流程"
          description="首页只做分发与信任背书，模块动作统一下沉到各自模块页。"
        />
        <div className="module-entry-grid">
          <ModuleEntryCard
            eyebrow="领养中心"
            title="查看可领养，或发布送养"
            description="面向领养人与送养人，统一管理申请和发布流程。"
            links={[
              { label: "查看可领养", to: "/adoption/list" },
              { label: "发布送养", to: "/adoption/post/new" },
              { label: "我的申请", to: "/adoption/applications" },
              { label: "我的送养", to: "/adoption/my-posts" }
            ]}
            primaryAction={{ label: "进入领养", to: "/adoption" }}
          />
          <ModuleEntryCard
            eyebrow="上门喂养"
            title="主人与服务者双角色流程"
            description="主人侧重找人下单，服务者侧重接单服务与留痕。"
            links={[
              { label: "找喂养服务者（主人）", to: "/feeding/providers" },
              { label: "发起喂养单（主人）", to: "/feeding/orders/new" },
              { label: "服务者中心（服务者）", to: "/feeding/provider-center" },
              { label: "我的接单（服务者）", to: "/feeding/my-jobs" }
            ]}
            primaryAction={{ label: "进入喂养", to: "/feeding" }}
            delay="70ms"
          />
        </div>
      </section>

      <section>
        <SectionHeader
          eyebrow="流程说明"
          title="两条流程各自清晰"
          description="先理解路径，再进入对应模块执行具体动作。"
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
            <h3>喂养流程</h3>
            <ol>
              {feedingFlowSteps.map((step) => (
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
            title="推荐喂养服务者"
            description="按服务记录、评分与响应速度综合推荐。"
            items={feedingProviderPreviewItems}
            action={{ label: "查看全部服务者", to: "/feeding/providers" }}
          />
          <FeaturePreviewList
            title="救助指引快捷入口"
            description="遇到突发情况先看指引，再决定下一步。"
            items={rescueQuickLinks}
            action={{ label: "进入救助模块", to: "/rescue" }}
          />
        </div>
      </section>

      <section className="card trust-footer-panel fade-up">
        <div className="trust-footer-header">
          <p className="eyebrow">平台保障</p>
          <h3>温暖治愈，也要专业可信</h3>
          <p>投诉、审核、线索处理和服务留痕都应有明确闭环。</p>
        </div>
        <div className="trust-badge-row">
          {homepageTrustBadges.map((item) => (
            <TrustBadge key={item} text={item} />
          ))}
        </div>
        <div className="trust-footer-actions">
          <Link className="primary-btn" to="/support/complaints/new">
            投诉与保障入口
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
