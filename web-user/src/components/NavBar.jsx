import { NavLink } from "react-router-dom";

const links = [
  { to: "/", label: "首页" },
  { to: "/adoption", label: "领养列表" },
  { to: "/feeding/providers", label: "喂养服务者" },
  { to: "/feeding/orders/new", label: "发起喂养单" },
  { to: "/me/feeding/orders", label: "我的喂养单" },
  { to: "/provider/feeding/profile", label: "服务者中心" },
  { to: "/provider/feeding/orders", label: "我的接单" },
  { to: "/adoption/rehome/new", label: "发布送养" },
  { to: "/me/adoption/posts", label: "我的发布" },
  { to: "/me/adoption/applications", label: "我的申请" },
  { to: "/admin/adoptions/posts", label: "审核后台" },
  { to: "/services", label: "上门喂养服务" }
];

function NavBar() {
  function setupToken() {
    const tokenType = window.prompt("设置 token 类型（user/admin）", "user");
    if (!tokenType) {
      return;
    }
    const normalized = tokenType.trim().toLowerCase();
    if (normalized !== "user" && normalized !== "admin") {
      window.alert("仅支持 user 或 admin");
      return;
    }
    const token = window.prompt(`请输入 ${normalized} access token`, "");
    if (!token) {
      return;
    }
    const key =
      normalized === "admin"
        ? "petlove_admin_access_token"
        : "petlove_user_access_token";
    window.localStorage.setItem(key, token.trim());
    window.alert(`${normalized} token 已保存到 localStorage`);
  }

  return (
    <header className="site-header">
      <div className="container nav-wrap">
        <NavLink to="/" className="brand">
          <span className="brand-badge">PL</span>
          <span className="brand-text">PetLove</span>
        </NavLink>
        <nav className="site-nav">
          {links.map((link) => (
            <NavLink
              key={link.to}
              to={link.to}
              className={({ isActive }) =>
                `nav-link ${isActive ? "nav-link-active" : ""}`
              }
            >
              {link.label}
            </NavLink>
          ))}
        </nav>
        <button className="ghost-btn" type="button" onClick={setupToken}>
          设置 Token
        </button>
      </div>
    </header>
  );
}

export default NavBar;
