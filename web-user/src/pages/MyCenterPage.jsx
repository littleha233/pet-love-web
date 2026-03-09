import { useState } from "react";
import { Link } from "react-router-dom";
import ModuleEntryCard from "../components/ModuleEntryCard.jsx";

const PROFILE_STORAGE_KEY = "petlove_user_profile";

function randomText(length = 8) {
  const chars = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";
  let value = "";
  for (let i = 0; i < length; i += 1) {
    value += chars[Math.floor(Math.random() * chars.length)];
  }
  return value;
}

function buildDefaultProfile() {
  return {
    username: `user_${randomText(6)}`,
    accountId: `PL-${randomText(10)}`,
    bio: ""
  };
}

function loadProfile() {
  try {
    const raw = window.localStorage.getItem(PROFILE_STORAGE_KEY);
    if (!raw) {
      const created = buildDefaultProfile();
      window.localStorage.setItem(PROFILE_STORAGE_KEY, JSON.stringify(created));
      return created;
    }
    const parsed = JSON.parse(raw);
    const normalized = {
      username: parsed?.username?.trim() || `user_${randomText(6)}`,
      accountId: parsed?.accountId?.trim() || `PL-${randomText(10)}`,
      bio: parsed?.bio || ""
    };
    window.localStorage.setItem(PROFILE_STORAGE_KEY, JSON.stringify(normalized));
    return normalized;
  } catch (error) {
    const fallback = buildDefaultProfile();
    window.localStorage.setItem(PROFILE_STORAGE_KEY, JSON.stringify(fallback));
    return fallback;
  }
}

function MyCenterPage() {
  const [profile, setProfile] = useState(() => loadProfile());
  const [usernameInput, setUsernameInput] = useState(profile.username);
  const [bioInput, setBioInput] = useState(profile.bio);
  const [notice, setNotice] = useState("");

  function onSaveProfile(event) {
    event.preventDefault();
    const username = usernameInput.trim() || `user_${randomText(6)}`;
    const nextProfile = {
      ...profile,
      username,
      bio: bioInput.trim()
    };
    setProfile(nextProfile);
    setUsernameInput(username);
    setBioInput(nextProfile.bio);
    window.localStorage.setItem(PROFILE_STORAGE_KEY, JSON.stringify(nextProfile));
    setNotice("已保存个人信息");
  }

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

      <section className="card page-form-card fade-up" style={{ "--delay": "90ms" }}>
        <h3>个人信息</h3>
        <form className="stack-form profile-info-form" onSubmit={onSaveProfile}>
          <label>
            用户名
            <input
              value={usernameInput}
              onChange={(event) => setUsernameInput(event.target.value)}
              maxLength={32}
              placeholder="例如：user_A8K2QX"
            />
          </label>
          <label>
            个人简介（选填）
            <input
              value={bioInput}
              onChange={(event) => setBioInput(event.target.value)}
              maxLength={80}
              placeholder="介绍一下你和宠物"
            />
          </label>
          <div className="profile-meta-grid">
            <p>账号 ID：{profile.accountId}</p>
            <p>当前用户名：{profile.username}</p>
          </div>
          <div className="action-row">
            <button className="primary-btn" type="submit">
              保存信息
            </button>
          </div>
          {notice ? <p className="helper-text notice-text">{notice}</p> : null}
        </form>
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
