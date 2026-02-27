import { Link } from "react-router-dom";

function FeaturePreviewList({ title, description, items, action }) {
  return (
    <article className="card feature-preview-card fade-up">
      <h3>{title}</h3>
      <p>{description}</p>
      <ul className="feature-preview-list">
        {items.map((item) => (
          <li key={item.title}>
            <Link to={item.to}>{item.title}</Link>
            <p>{item.subtitle}</p>
          </li>
        ))}
      </ul>
      {action ? (
        <Link className="secondary-btn feature-preview-action" to={action.to}>
          {action.label}
        </Link>
      ) : null}
    </article>
  );
}

export default FeaturePreviewList;
