import { Link } from "react-router-dom";

const SCENARIOS = [
  { code: "FOUND_STRAY_CAT", title: "发现流浪猫", desc: "先判断安全，再准备食物与临时安置。" },
  { code: "FOUND_STRAY_DOG", title: "发现流浪狗", desc: "优先避免追赶和刺激，评估是否可接近。" },
  { code: "INJURED_CAT", title: "发现受伤猫", desc: "先止血与保暖，再尽快联系医院。" },
  { code: "INJURED_DOG", title: "发现受伤狗", desc: "注意防护与转运方式，降低二次伤害风险。" },
  { code: "ABANDONED_KITTENS", title: "幼猫救助", desc: "先确认是否母猫暂离，再决定是否带离。" },
  { code: "ABANDONED_PUPPIES", title: "幼犬救助", desc: "关注保暖补液与喂养频率，尽快联系资源。" }
];

function RescueHomePage() {
  return (
    <div className="page-stack">
      <section className="card page-banner fade-up">
        <p className="eyebrow">宠物救助</p>
        <h1>先看指引，再找资源，必要时提交线索</h1>
      </section>

      <section className="card page-form-card fade-up" style={{ "--delay": "60ms" }}>
        <div className="action-row">
          <Link className="primary-btn" to="/rescue/guides">
            查看救助指引
          </Link>
          <Link className="secondary-btn" to="/rescue/resources">
            查看本地资源目录
          </Link>
          <Link className="secondary-btn" to="/rescue/clues/new">
            提交救助线索
          </Link>
          <Link className="secondary-btn" to="/me/rescue/clues">
            我的线索
          </Link>
          <Link className="secondary-btn" to="/support/complaints/new">
            提交问题反馈
          </Link>
        </div>
      </section>

      <section>
        <h2>常见场景入口</h2>
        <div className="adoption-post-grid">
          {SCENARIOS.map((item) => (
            <article key={item.code} className="card adoption-post-content">
              <h3 className="adoption-post-title">{item.title}</h3>
              <p className="adoption-post-meta">{item.desc}</p>
              <div className="action-row">
                <Link className="secondary-btn" to={`/rescue/guides?scenarioCode=${item.code}`}>
                  看对应指引
                </Link>
              </div>
            </article>
          ))}
        </div>
      </section>
    </div>
  );
}

export default RescueHomePage;
