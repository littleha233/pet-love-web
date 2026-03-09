import { NavLink, Link, useLocation, useNavigate } from "react-router-dom";

const links = [
  { to: "/", label: "首页", end: true },
  { to: "/adoption", label: "领养" },
  { to: "/rescue", label: "救助" },
  { to: "/community", label: "社区" },
  { to: "/me", label: "我的" }
];
const USER_ACCESS_TOKEN_KEY = "petlove_user_access_token";
const USER_REFRESH_TOKEN_KEY = "petlove_user_refresh_token";
const ADMIN_ACCESS_TOKEN_KEY = "petlove_admin_access_token";
const ADMIN_REFRESH_TOKEN_KEY = "petlove_admin_refresh_token";

function NavBar() {
  const navigate = useNavigate();
  const location = useLocation();

  const userToken = window.localStorage.getItem(USER_ACCESS_TOKEN_KEY);
  const hasLoginState = Boolean(userToken);

  function onLogout() {
    if (!hasLoginState) {
      return;
    }
    window.localStorage.removeItem(USER_ACCESS_TOKEN_KEY);
    window.localStorage.removeItem(USER_REFRESH_TOKEN_KEY);
    window.localStorage.removeItem(ADMIN_ACCESS_TOKEN_KEY);
    window.localStorage.removeItem(ADMIN_REFRESH_TOKEN_KEY);
    navigate("/", { replace: false, state: { notice: "已退出登录" } });
  }

  return (
    <header className="site-header">
      <div className="container nav-wrap">
        <NavLink to="/" className="brand">
          <span className="brand-badge" aria-hidden="true">
            <img className="brand-cat-icon" src="/favicon-cat.svg" alt="" />
          </span>
          <span className="brand-text">PetLove</span>
        </NavLink>
        <nav className="site-nav">
          {links.map((link) => (
            <NavLink
              key={link.to}
              to={link.to}
              end={Boolean(link.end)}
              className={({ isActive }) =>
                `nav-link ${isActive ? "nav-link-active" : ""}`
              }
            >
              {link.label}
            </NavLink>
          ))}
        </nav>

        <div className="auth-tag-row">
          <Link
            className={`auth-tag ${location.pathname === "/auth/mobile-login" ? "auth-tag-active" : ""}`}
            to="/auth/mobile-login?intent=register"
          >
            注册
          </Link>
          <Link
            className={`auth-tag ${location.pathname === "/auth/mobile-login" ? "auth-tag-active" : ""}`}
            to="/auth/mobile-login"
          >
            登录
          </Link>
          <button
            type="button"
            className="auth-tag auth-tag-danger"
            disabled={!hasLoginState}
            onClick={onLogout}
          >
            登出
          </button>
        </div>
      </div>
    </header>
  );
}

export default NavBar;
