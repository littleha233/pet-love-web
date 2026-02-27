import { Link } from "react-router-dom";

function RoleEntryCard({
  role,
  title,
  description,
  entries,
  primaryAction,
  delay = "0ms"
}) {
  return (
    <article className="card role-entry-card fade-up" style={{ "--delay": delay }}>
      <p className="eyebrow">{role}</p>
      <h3>{title}</h3>
      <p>{description}</p>
      <ul className="role-entry-list">
        {entries.map((entry) => (
          <li key={entry.to}>
            <Link to={entry.to}>{entry.label}</Link>
          </li>
        ))}
      </ul>
      <Link className="secondary-btn role-entry-cta" to={primaryAction.to}>
        {primaryAction.label}
      </Link>
    </article>
  );
}

export default RoleEntryCard;
