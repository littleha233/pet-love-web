import { NavLink } from "react-router-dom";

const links = [
  { to: "/", label: "首页" },
  { to: "/adoption", label: "领养列表" },
  { to: "/services", label: "上门喂养服务" }
];

function NavBar() {
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
        <button className="ghost-btn" type="button">
          登录 / 注册
        </button>
      </div>
    </header>
  );
}

export default NavBar;
