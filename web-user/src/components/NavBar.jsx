import { useEffect, useRef, useState } from "react";
import { NavLink, Link, useLocation } from "react-router-dom";

const links = [
  { to: "/", label: "首页", end: true },
  { to: "/adoption", label: "领养" },
  { to: "/rescue", label: "救助" },
  { to: "/community", label: "社区" },
  { to: "/me", label: "我的" }
];
const MENU_AUTO_CLOSE_MS = 3200;

function NavBar() {
  const location = useLocation();
  const menuRef = useRef(null);
  const [menuOpen, setMenuOpen] = useState(false);

  const userToken = window.localStorage.getItem("petlove_user_access_token");
  const hasLoginState = Boolean(userToken);

  useEffect(() => {
    function onPointerDown(event) {
      if (!menuRef.current) {
        return;
      }
      if (!menuRef.current.contains(event.target)) {
        setMenuOpen(false);
      }
    }

    function onWindowBlur() {
      setMenuOpen(false);
    }

    document.addEventListener("pointerdown", onPointerDown, true);
    window.addEventListener("blur", onWindowBlur);
    return () => {
      document.removeEventListener("pointerdown", onPointerDown, true);
      window.removeEventListener("blur", onWindowBlur);
    };
  }, []);

  useEffect(() => {
    setMenuOpen(false);
  }, [location.pathname]);

  useEffect(() => {
    if (!menuOpen) {
      return undefined;
    }
    const timer = window.setTimeout(() => {
      setMenuOpen(false);
    }, MENU_AUTO_CLOSE_MS);

    function onKeyDown(event) {
      if (event.key === "Escape") {
        setMenuOpen(false);
      }
    }

    document.addEventListener("keydown", onKeyDown);
    return () => {
      window.clearTimeout(timer);
      document.removeEventListener("keydown", onKeyDown);
    };
  }, [menuOpen]);

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

        <div className="user-menu" ref={menuRef}>
          <button
            className="user-menu-trigger"
            type="button"
            onClick={() => setMenuOpen((prev) => !prev)}
            aria-expanded={menuOpen}
            aria-haspopup="menu"
          >
            <span className="user-menu-avatar">{hasLoginState ? "我" : "登"}</span>
            <span className="user-menu-text">{hasLoginState ? "账号" : "登录"}</span>
          </button>
          {menuOpen ? (
            <div className="user-menu-panel">
              <Link className="user-menu-item" to="/auth/mobile-login" onClick={() => setMenuOpen(false)}>
                短信登录
              </Link>
              <Link className="user-menu-item" to="/me" onClick={() => setMenuOpen(false)}>
                我的中心
              </Link>
            </div>
          ) : null}
        </div>
      </div>
    </header>
  );
}

export default NavBar;
