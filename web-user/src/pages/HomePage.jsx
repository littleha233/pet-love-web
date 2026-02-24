import Hero from "../components/Hero.jsx";
import SectionHeader from "../components/SectionHeader.jsx";
import PetCard from "../components/PetCard.jsx";
import ServiceCard from "../components/ServiceCard.jsx";
import TrustModule from "../components/TrustModule.jsx";
import FaqList from "../components/FaqList.jsx";
import {
  featuredPets,
  homeFaq,
  serviceCards,
  timelineSample,
  trustPoints
} from "../data/mockData.js";

function HomePage() {
  return (
    <div className="page-stack">
      <Hero
        title="帮助它找到家，也让托付更安心"
        subtitle="一个以领养、上门喂养、救助指引为核心的宠物服务平台。我们坚持温暖表达，也坚持流程透明与可追溯。"
        imageUrl="https://images.pexels.com/photos/4587994/pexels-photo-4587994.jpeg?auto=compress&cs=tinysrgb&w=1200"
        imageAlt="主人与宠物互动"
        primaryText="查看可领养宠物"
        primaryTo="/adoption"
        secondaryText="了解喂养服务"
        secondaryTo="/services"
        highlights={["已审核信息", "服务留痕可查", "投诉处理闭环"]}
      />

      <section>
        <SectionHeader
          eyebrow="领养 / 送养"
          title="真实信息，减少等待与误会"
          description="所有展示内容以真实照片与基础审核为先，避免过度包装。"
        />
        <div className="card-grid">
          {featuredPets.map((pet, index) => (
            <PetCard key={pet.id} pet={pet} index={index} />
          ))}
        </div>
      </section>

      <section>
        <SectionHeader
          eyebrow="上门喂养"
          title="把照护流程做得像清单一样明确"
          description="从预约到完成，节点和记录都在一个页面清晰呈现。"
        />
        <div className="card-grid card-grid-three">
          {serviceCards.map((service, index) => (
            <ServiceCard key={service.title} service={service} index={index} />
          ))}
        </div>
      </section>

      <TrustModule points={trustPoints} timeline={timelineSample} />
      <FaqList items={homeFaq} />
    </div>
  );
}

export default HomePage;
