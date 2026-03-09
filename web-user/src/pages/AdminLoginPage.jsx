import { useEffect, useState } from "react";
import { Link, useLocation, useNavigate, useSearchParams } from "react-router-dom";
import { adminPasswordLogin } from "../api/authApi";

const ADMIN_ACCESS_TOKEN_KEY = "petlove_admin_access_token";
const ADMIN_REFRESH_TOKEN_KEY = "petlove_admin_refresh_token";

function AdminLoginPage() {
  const navigate = useNavigate();
  const location = useLocation();
  const [searchParams] = useSearchParams();
  const [username, setUsername] = useState("");
  const [password, setPassword] = useState("");
  const [submitting, setSubmitting] = useState(false);
  const [notice, setNotice] = useState("");

  const rawRedirect = searchParams.get("redirect");
  const redirectPath =
    rawRedirect && rawRedirect.startsWith("/admin/") ? rawRedirect : "/admin/adoptions/posts";

  useEffect(() => {
    const hasAdminToken = Boolean(window.localStorage.getItem(ADMIN_ACCESS_TOKEN_KEY));
    if (hasAdminToken) {
      navigate(redirectPath, { replace: true });
    }
  }, [navigate, redirectPath]);

  useEffect(() => {
    if (location.state?.notice) {
      setNotice(location.state.notice);
    }
  }, [location.state]);

  async function onSubmit(event) {
    event.preventDefault();
    setSubmitting(true);
    setNotice("");

    try {
      const data = await adminPasswordLogin({
        username: username.trim(),
        password
      });
      window.localStorage.setItem(ADMIN_ACCESS_TOKEN_KEY, data.accessToken);
      window.localStorage.setItem(ADMIN_REFRESH_TOKEN_KEY, data.refreshToken);
      navigate(redirectPath, {
        replace: true,
        state: { notice: "管理员登录成功" }
      });
    } catch (err) {
      setNotice(err.message || "登录失败，请重试");
    } finally {
      setSubmitting(false);
    }
  }

  return (
    <div className="page-stack admin-login-page">
      <section className="card page-banner">
        <p className="eyebrow">管理员入口</p>
        <h1>后台审核登录</h1>
      </section>

      <section className="card page-form-card">
        <form className="stack-form" onSubmit={onSubmit}>
          <label>
            管理员账号
            <input
              name="username"
              value={username}
              onChange={(event) => setUsername(event.target.value)}
              placeholder="请输入管理员账号"
              maxLength={64}
              required
            />
          </label>
          <label>
            密码
            <input
              name="password"
              type="password"
              value={password}
              onChange={(event) => setPassword(event.target.value)}
              placeholder="请输入管理员密码"
              maxLength={128}
              required
            />
          </label>

          <button className="primary-btn" type="submit" disabled={submitting}>
            {submitting ? "登录中..." : "登录后台"}
          </button>

          {notice ? <p className="error-text">{notice}</p> : null}
          <p className="helper-text">
            <Link to="/">返回用户首页</Link>
          </p>
        </form>
      </section>
    </div>
  );
}

export default AdminLoginPage;
