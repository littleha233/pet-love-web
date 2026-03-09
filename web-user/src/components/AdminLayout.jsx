import { Link, NavLink, Navigate, Outlet, useLocation, useNavigate } from "react-router-dom";

const ADMIN_ACCESS_TOKEN_KEY = "petlove_admin_access_token";
const ADMIN_REFRESH_TOKEN_KEY = "petlove_admin_refresh_token";

function AdminLayout() {
  const location = useLocation();
  const navigate = useNavigate();
  const hasAdminToken = Boolean(window.localStorage.getItem(ADMIN_ACCESS_TOKEN_KEY));

  if (!hasAdminToken) {
    const query = new URLSearchParams();
    query.set("redirect", `${location.pathname}${location.search}`);
    return <Navigate to={`/admin/login?${query.toString()}`} replace />;
  }

  function onAdminLogout() {
    window.localStorage.removeItem(ADMIN_ACCESS_TOKEN_KEY);
    window.localStorage.removeItem(ADMIN_REFRESH_TOKEN_KEY);
    navigate("/admin/login", {
      replace: true,
      state: { notice: "已退出管理员账号" }
    });
  }

  return (
    <div className="admin-shell">
      <header className="admin-header">
        <div className="container admin-header-wrap">
          <div className="admin-brand">
            <span className="admin-badge">ADMIN</span>
            <span>PetLove 审核后台</span>
          </div>
          <nav className="admin-nav">
            <NavLink
              to="/admin/adoptions/posts"
              className={({ isActive }) => `admin-nav-link ${isActive ? "admin-nav-link-active" : ""}`}
            >
              帖子审核
            </NavLink>
            <Link className="admin-nav-link" to="/">
              用户首页
            </Link>
            <button type="button" className="ghost-btn" onClick={onAdminLogout}>
              退出后台
            </button>
          </nav>
        </div>
      </header>

      <main className="page-main container admin-main">
        <Outlet />
      </main>
    </div>
  );
}

export default AdminLayout;
