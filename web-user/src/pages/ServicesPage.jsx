import SectionHeader from "../components/SectionHeader.jsx";
import ServiceCard from "../components/ServiceCard.jsx";
import TrustModule from "../components/TrustModule.jsx";
import FaqList from "../components/FaqList.jsx";
import {
  processSteps,
  serviceCards,
  serviceFaq,
  timelineSample,
  trustPoints
} from "../data/mockData.js";

function ServicesPage() {
  return (
    <div className="page-stack">
      <section className="card page-banner fade-up">
        <p className="eyebrow">上门喂养服务</p>
        <h1>温和沟通 + 标准流程，才能真正建立信任</h1>
        <p>
          平台首版聚焦“预约、留痕、投诉处理”闭环，不做花哨噱头，不省略关键步骤。
        </p>
      </section>

      <section>
        <SectionHeader
          eyebrow="服务流程"
          title="四步完成一次可追踪的上门服务"
          description="流程模块化展示，便于后续接真实状态机。"
        />
        <ol className="process-grid">
          {processSteps.map((step, index) => (
            <li key={step} className="card fade-up" style={{ "--delay": `${index * 80}ms` }}>
              <span>0{index + 1}</span>
              <p>{step}</p>
            </li>
          ))}
        </ol>
      </section>

      <section>
        <SectionHeader
          eyebrow="服务套餐"
          title="根据宠物习性选择照护深度"
          description="价格与服务范围清晰展示，减少沟通不对齐。"
        />
        <div className="card-grid card-grid-three">
          {serviceCards.map((service, index) => (
            <ServiceCard key={service.title} service={service} index={index} />
          ))}
        </div>
      </section>

      <TrustModule points={trustPoints} timeline={timelineSample} />
      <FaqList items={serviceFaq} />
    </div>
  );
}

export default ServicesPage;
