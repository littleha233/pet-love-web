import { Link } from "react-router-dom";

const SHARE_POSTS = [
  {
    id: "S-2401",
    title: "从救助到领养：奶糖到家的第 30 天",
    author: "杭州 · 林夏",
    summary: "先做体检，再循序适应新环境。头两周重点是作息和安全感建立。",
    tags: ["领养日记", "新手指南", "猫咪"]
  },
  {
    id: "S-2402",
    title: "夜间发现受伤流浪犬，我是这样处理的",
    author: "深圳 · 陈泽",
    summary: "先远距观察，再联系 24 小时医院和本地志愿者，整个过程控制在 40 分钟内。",
    tags: ["救助经验", "紧急处理", "狗狗"]
  },
  {
    id: "S-2403",
    title: "送养信息怎么写更容易匹配到合适家庭",
    author: "上海 · 周宁",
    summary: "把健康情况、作息习惯、可接触人群写清楚，能明显减少无效沟通。",
    tags: ["送养建议", "信息完善", "实操"]
  },
  {
    id: "S-2404",
    title: "幼猫临时安置清单（可直接照着准备）",
    author: "南京 · 叶子",
    summary: "保暖箱、奶粉、喂食器和隔离垫是必需品，体温和排泄记录建议每天两次。",
    tags: ["幼猫救助", "物资清单", "经验贴"]
  }
];

const HOT_TOPICS = ["领养回访", "救助资源避坑", "线索跟进", "家庭适配评估", "新手喂养"];

function CommunityPage() {
  return (
    <div className="page-stack">
      <section className="card page-banner fade-up">
        <p className="eyebrow">社区</p>
        <h1>宠物故事与经验分享</h1>
      </section>

      <section className="card page-form-card fade-up" style={{ "--delay": "60ms" }}>
        <p className="helper-text">热门话题</p>
        <div className="tag-row">
          {HOT_TOPICS.map((topic) => (
            <span key={topic} className="soft-tag soft-tag-active">
              #{topic}
            </span>
          ))}
        </div>
      </section>

      <section className="community-grid">
        {SHARE_POSTS.map((post, index) => (
          <article
            key={post.id}
            className="card community-post-card fade-up"
            style={{ "--delay": `${80 + index * 50}ms` }}
          >
            <div className="status-line">
              <span className="soft-tag">分享</span>
              <span>{post.author}</span>
            </div>
            <h3>{post.title}</h3>
            <p>{post.summary}</p>
            <div className="tag-row">
              {post.tags.map((tag) => (
                <span key={tag} className="soft-tag">
                  {tag}
                </span>
              ))}
            </div>
            <div className="action-row">
              <Link className="secondary-btn" to="/adoption">
                去领养模块
              </Link>
              <Link className="secondary-btn" to="/rescue">
                去救助模块
              </Link>
            </div>
          </article>
        ))}
      </section>
    </div>
  );
}

export default CommunityPage;
