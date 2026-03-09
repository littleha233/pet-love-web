import { Link } from "react-router-dom";

function ModuleEntryCard({
  eyebrow,
  title,
  links,
  primaryAction,
  delay = "0ms"
}) {
  return (
    <article className="card module-entry-card fade-up" style={{ "--delay": delay }}>
      <p className="eyebrow">{eyebrow}</p>
      <h3>{title}</h3>
      <div className="module-entry-links">
        {links.map((item) => (
          <Link key={item.to} className="module-link-chip" to={item.to}>
            {item.label}
          </Link>
        ))}
      </div>
      <Link className="primary-btn module-entry-cta" to={primaryAction.to}>
        {primaryAction.label}
      </Link>
    </article>
  );
}

export default ModuleEntryCard;
