import SectionHeader from "../components/SectionHeader.jsx";
import PetCard from "../components/PetCard.jsx";
import { adoptionPets } from "../data/mockData.js";

function AdoptionPage() {
  return (
    <div className="page-stack">
      <section className="card page-banner fade-up">
        <p className="eyebrow">领养列表</p>
        <h1>为每一次相遇保留足够的信息透明</h1>
        <p>
          首版不做复杂匹配算法，重点把“审核状态、宠物信息、申请动作”展示清楚。
        </p>
      </section>

      <section className="filter-bar card fade-up" style={{ "--delay": "80ms" }}>
        <button type="button" className="soft-tag soft-tag-active">
          全部城市
        </button>
        <button type="button" className="soft-tag">
          猫
        </button>
        <button type="button" className="soft-tag">
          狗
        </button>
        <button type="button" className="soft-tag">
          可申请
        </button>
        <button type="button" className="soft-tag">
          已审核
        </button>
      </section>

      <section>
        <SectionHeader
          eyebrow="信息卡片"
          title="每条信息都保留状态标签与关键说明"
          description="示例为静态 mock 数据，后续可直接替换为 API 返回。"
        />
        <div className="card-grid">
          {adoptionPets.map((pet, index) => (
            <PetCard key={pet.id} pet={pet} index={index} />
          ))}
        </div>
      </section>
    </div>
  );
}

export default AdoptionPage;
