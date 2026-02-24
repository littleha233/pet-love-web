import { Link } from "react-router-dom";

function Hero({
  title,
  subtitle,
  imageUrl,
  imageAlt,
  primaryText,
  primaryTo,
  secondaryText,
  secondaryTo,
  highlights
}) {
  return (
    <section className="hero card fade-up">
      <div className="hero-content">
        <p className="eyebrow">PetLove Web</p>
        <h1>{title}</h1>
        <p className="hero-subtitle">{subtitle}</p>
        <div className="hero-actions">
          <Link to={primaryTo} className="primary-btn">
            {primaryText}
          </Link>
          <Link to={secondaryTo} className="secondary-btn">
            {secondaryText}
          </Link>
        </div>
        <ul className="hero-highlights">
          {highlights.map((item) => (
            <li key={item}>{item}</li>
          ))}
        </ul>
      </div>
      <div className="hero-media">
        <img src={imageUrl} alt={imageAlt} />
      </div>
    </section>
  );
}

export default Hero;
