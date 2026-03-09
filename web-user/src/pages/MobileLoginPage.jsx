import { useEffect, useMemo, useRef, useState } from "react";
import { useNavigate, useSearchParams } from "react-router-dom";
import { mobileCodeLogin, sendMobileSmsCode } from "../api/authApi";

const USER_ACCESS_TOKEN_KEY = "petlove_user_access_token";
const USER_REFRESH_TOKEN_KEY = "petlove_user_refresh_token";
const DEVICE_ID_KEY = "petlove_device_id";
const CAPTCHA_CHARS = "23456789ABCDEFGHJKLMNPQRSTUVWXYZ";

function resolveDeviceId() {
  const cached = window.localStorage.getItem(DEVICE_ID_KEY);
  if (cached) {
    return cached;
  }
  const generated = `web_${Date.now().toString(36)}_${Math.random().toString(36).slice(2, 10)}`;
  window.localStorage.setItem(DEVICE_ID_KEY, generated);
  return generated;
}

function generateCaptcha(length = 4) {
  let result = "";
  for (let i = 0; i < length; i += 1) {
    result += CAPTCHA_CHARS[Math.floor(Math.random() * CAPTCHA_CHARS.length)];
  }
  return result;
}

function drawCaptcha(canvas, captchaCode) {
  if (!canvas) {
    return;
  }
  const ctx = canvas.getContext("2d");
  if (!ctx) {
    return;
  }

  const width = canvas.width;
  const height = canvas.height;

  ctx.fillStyle = "#f9f4ee";
  ctx.fillRect(0, 0, width, height);

  for (let i = 0; i < 5; i += 1) {
    ctx.strokeStyle = `rgba(145, 95, 61, ${0.25 + Math.random() * 0.25})`;
    ctx.lineWidth = 1;
    ctx.beginPath();
    ctx.moveTo(Math.random() * width, Math.random() * height);
    ctx.lineTo(Math.random() * width, Math.random() * height);
    ctx.stroke();
  }

  for (let i = 0; i < 32; i += 1) {
    ctx.fillStyle = "rgba(124, 95, 72, 0.22)";
    ctx.beginPath();
    ctx.arc(Math.random() * width, Math.random() * height, Math.random() * 1.5 + 0.4, 0, Math.PI * 2);
    ctx.fill();
  }

  ctx.font = "700 28px 'Fraunces', serif";
  ctx.textBaseline = "middle";
  const gap = width / (captchaCode.length + 1);
  for (let i = 0; i < captchaCode.length; i += 1) {
    const ch = captchaCode[i];
    const x = gap * (i + 1) - 9;
    const y = height / 2 + (Math.random() * 8 - 4);
    const rotate = (Math.random() * 16 - 8) * (Math.PI / 180);
    ctx.save();
    ctx.translate(x, y);
    ctx.rotate(rotate);
    ctx.fillStyle = "#7b4f33";
    ctx.fillText(ch, 0, 0);
    ctx.restore();
  }
}

function MobileLoginPage() {
  const navigate = useNavigate();
  const [searchParams] = useSearchParams();
  const deviceId = useMemo(() => resolveDeviceId(), []);
  const captchaCanvasRef = useRef(null);
  const isRegisterView = searchParams.get("intent") === "register";

  const [mobile, setMobile] = useState("");
  const [code, setCode] = useState("");
  const [captchaCode, setCaptchaCode] = useState(() => generateCaptcha(4));
  const [captchaInput, setCaptchaInput] = useState("");
  const [countdown, setCountdown] = useState(0);
  const [sending, setSending] = useState(false);
  const [loggingIn, setLoggingIn] = useState(false);
  const [notice, setNotice] = useState("");

  useEffect(() => {
    if (countdown <= 0) {
      return undefined;
    }
    const timer = window.setInterval(() => {
      setCountdown((prev) => (prev <= 1 ? 0 : prev - 1));
    }, 1000);
    return () => {
      window.clearInterval(timer);
    };
  }, [countdown]);

  useEffect(() => {
    drawCaptcha(captchaCanvasRef.current, captchaCode);
  }, [captchaCode]);

  function refreshCaptcha() {
    setCaptchaCode(generateCaptcha(4));
  }

  async function onSendCode(event) {
    event.preventDefault();
    if (countdown > 0) {
      return;
    }
    if (!mobile.trim()) {
      setNotice("请先输入手机号");
      return;
    }
    if (!captchaInput.trim()) {
      setNotice("请先输入图形验证码");
      return;
    }
    const lockedCaptchaInput = captchaInput;
    const lockedCaptchaCode = captchaCode;
    if (lockedCaptchaInput.trim().toUpperCase() !== lockedCaptchaCode) {
      setNotice("图形验证码不正确，请重试");
      return;
    }

    setSending(true);
    setNotice("");

    try {
      const data = await sendMobileSmsCode({
        mobile,
        bizType: "LOGIN",
        deviceId,
        captchaToken: `local-captcha:${lockedCaptchaCode.toLowerCase()}`
      });
      const nextCooldown = Number(data?.cooldownSeconds || 60);
      setCountdown(Number.isFinite(nextCooldown) && nextCooldown > 0 ? nextCooldown : 60);
      setNotice("验证码已发送，请注意查收短信");
    } catch (err) {
      setNotice(err.message || "发送验证码失败");
    } finally {
      setSending(false);
      setCaptchaInput(lockedCaptchaInput);
      setCaptchaCode(lockedCaptchaCode);
    }
  }

  async function onLogin(event) {
    event.preventDefault();
    setLoggingIn(true);
    setNotice("");

    try {
      const data = await mobileCodeLogin({
        mobile,
        code,
        deviceId
      });

      window.localStorage.setItem(USER_ACCESS_TOKEN_KEY, data.accessToken);
      window.localStorage.setItem(USER_REFRESH_TOKEN_KEY, data.refreshToken);

      const welcomeText = data.isNewUser
        ? "登录成功，已自动创建账号"
        : "登录成功";
      setNotice(welcomeText);

      navigate("/", { replace: true, state: { notice: welcomeText } });
    } catch (err) {
      setNotice(err.message || "登录失败");
    } finally {
      setLoggingIn(false);
    }
  }

  return (
    <div className="page-stack auth-login-page">
      <section className="auth-login-shell fade-up">
        <aside className="card auth-login-intro">
          <p className="eyebrow">{isRegisterView ? "账号注册" : "账号登录"}</p>
          <h1>{isRegisterView ? "手机号快速注册" : "手机号快捷登录"}</h1>
          <ul className="auth-login-tags">
            <li>短信登录</li>
            <li>自动注册</li>
            <li>安全保护</li>
          </ul>
        </aside>

        <section className="card auth-login-form-card" style={{ "--delay": "70ms" }}>
          <form className="stack-form auth-login-form" onSubmit={onLogin}>
            <label>
              手机号
              <div className="auth-mobile-row">
                <span className="auth-mobile-prefix">+86</span>
                <input
                  className="auth-mobile-input"
                  value={mobile}
                  onChange={(event) => setMobile(event.target.value)}
                  placeholder="13800138000"
                  maxLength={20}
                  required
                />
              </div>
            </label>

            <div className="auth-captcha-block">
              <label className="auth-captcha-label">
                图形验证码
                <input
                  className="auth-captcha-input"
                  value={captchaInput}
                  onChange={(event) => setCaptchaInput(event.target.value)}
                  placeholder="输入图形验证码"
                  maxLength={6}
                  required
                />
              </label>
              <div className="auth-captcha-canvas-wrap">
                <canvas ref={captchaCanvasRef} width="148" height="52" aria-label="图形验证码" />
                <button
                  className="ghost-btn auth-captcha-refresh"
                  type="button"
                  onClick={refreshCaptcha}
                  disabled={sending}
                >
                  看不清，换一张
                </button>
              </div>
            </div>

            <div className="auth-code-row">
              <label className="auth-code-input-label">
                短信验证码
                <input
                  value={code}
                  onChange={(event) => setCode(event.target.value)}
                  placeholder="6 位数字"
                  maxLength={8}
                  required
                />
              </label>
              <button
                className="secondary-btn auth-send-btn"
                type="button"
                onClick={onSendCode}
                disabled={sending || countdown > 0 || !mobile.trim() || !captchaInput.trim()}
              >
                {sending ? "发送中..." : countdown > 0 ? `${countdown}s 后重发` : "发送验证码"}
              </button>
            </div>

            <button className="primary-btn auth-submit-btn" type="submit" disabled={loggingIn || sending}>
              {loggingIn ? "处理中..." : isRegisterView ? "注册并登录" : "登录"}
            </button>

            {notice ? <p className="helper-text notice-text">{notice}</p> : null}
          </form>
        </section>
      </section>
    </div>
  );
}

export default MobileLoginPage;
